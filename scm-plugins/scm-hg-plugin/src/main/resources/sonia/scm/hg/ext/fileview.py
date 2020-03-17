#
# MIT License
#
# Copyright (c) 2020-present Cloudogu GmbH and Contributors
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#

"""fileview

Prints date, size and last message of files.
"""

from collections import defaultdict
from mercurial import scmutil

cmdtable = {}

try:
    from mercurial import registrar
    command = registrar.command(cmdtable)
except (AttributeError, ImportError):
    # Fallback to hg < 4.3 support
    from mercurial import cmdutil
    command = cmdutil.command(cmdtable)

try:
    from mercurial.utils import dateutil
    _parsedate = dateutil.parsedate
except ImportError:
    # compat with hg < 4.6
    from mercurial import util
    _parsedate = util.parsedate

FILE_MARKER = '<files>'

class File_Collector:

  def __init__(self, recursive = False):
    self.recursive = recursive
    self.structure = defaultdict(dict, ((FILE_MARKER, []),))

  def collect(self, paths, path = "", dir_only = False):
    for p in paths:
      if p.startswith(path):
        self.attach(self.extract_name_without_parent(path, p), self.structure, dir_only)

  def attach(self, branch, trunk, dir_only = False):
    parts = branch.split('/', 1)
    if len(parts) == 1:  # branch is a file
      if dir_only:
        trunk[parts[0]] = defaultdict(dict, ((FILE_MARKER, []),))
      else:
        trunk[FILE_MARKER].append(parts[0])
    else:
      node, others = parts
      if node not in trunk:
        trunk[node] = defaultdict(dict, ((FILE_MARKER, []),))
      if self.recursive:
        self.attach(others, trunk[node], dir_only)

  def extract_name_without_parent(self, parent, name_with_parent):
    if len(parent) > 0:
      name_without_parent = name_with_parent[len(parent):]
      if name_without_parent.startswith("/"):
          name_without_parent = name_without_parent[1:]
      return name_without_parent
    return name_with_parent

class File_Object:
  def __init__(self, directory, path):
    self.directory = directory
    self.path = path
    self.children = []
    self.sub_repository = None

  def get_name(self):
    parts = self.path.split("/")
    return parts[len(parts) - 1]

  def get_parent(self):
    idx = self.path.rfind("/")
    if idx > 0:
      return self.path[0:idx]
    return ""

  def add_child(self, child):
    self.children.append(child)

  def __getitem__(self, key):
    return self.children[key]

  def __len__(self):
    return len(self.children)

  def __repr__(self):
    result = self.path
    if self.directory:
      result += "/"
    return result

class File_Walker:

  def __init__(self, sub_repositories, visitor):
    self.visitor = visitor
    self.sub_repositories = sub_repositories

  def create_file(self, path):
    return File_Object(False, path)

  def create_directory(self, path):
    directory = File_Object(True, path)
    if path in self.sub_repositories:
      directory.sub_repository = self.sub_repositories[path]
    return directory

  def visit_file(self, path):
    file = self.create_file(path)
    self.visit(file)

  def visit_directory(self, path):
    file = self.create_directory(path)
    self.visit(file)

  def visit(self, file):
    self.visitor.visit(file)

  def create_path(self, parent, path):
    if len(parent) > 0:
      return parent + "/" + path
    return path

  def walk(self, structure, parent = ""):
    sortedItems = sorted(structure.iteritems(), key = lambda item: self.sortKey(item))
    for key, value in sortedItems:
      if key == FILE_MARKER:
        if value:
          for v in value:
            self.visit_file(self.create_path(parent, v))
      else:
        self.visit_directory(self.create_path(parent, key))
        if isinstance(value, dict):
          self.walk(value, self.create_path(parent, key))
        else:
          self.visit_directory(self.create_path(parent, value))

  def sortKey(self, item):
    if (item[0] == FILE_MARKER):
      return "2"
    else:
      return "1" + item[0]

class SubRepository:
  url = None
  revision = None

def collect_sub_repositories(revCtx):
  subrepos = {}
  try:
    hgsub = revCtx.filectx('.hgsub').data().split('\n')
    for line in hgsub:
      parts = line.split('=')
      if len(parts) > 1:
        subrepo = SubRepository()
        subrepo.url = parts[1].strip()
        subrepos[parts[0].strip()] = subrepo
  except Exception:
    pass

  try:
    hgsubstate = revCtx.filectx('.hgsubstate').data().split('\n')
    for line in hgsubstate:
      parts = line.split(' ')
      if len(parts) > 1:
        subrev = parts[0].strip()
        subrepo = subrepos[parts[1].strip()]
        subrepo.revision = subrev
  except Exception:
    pass

  return subrepos

class Writer:
  def __init__(self, ui):
    self.ui = ui

  def write(self, value):
    self.ui.write(value)

class File_Printer:

  def __init__(self, writer, repo, revCtx, disableLastCommit, transport, limit, offset):
    self.writer = writer
    self.repo = repo
    self.revCtx = revCtx
    self.disableLastCommit = disableLastCommit
    self.transport = transport
    self.result_count = 0
    self.initial_path_printed = False
    self.limit = limit
    self.offset = offset

  def print_directory(self, path):
    if not self.initial_path_printed or self.offset == 0 or self.shouldPrintResult():
      self.initial_path_printed = True
      format = '%s/\n'
      if self.transport:
          format = 'd%s/\0'
      self.writer.write( format % path)

  def print_file(self, path):
    self.result_count += 1
    if self.shouldPrintResult():
      file = self.revCtx[path]
      date = '0 0'
      description = 'n/a'
      if not self.disableLastCommit:
        linkrev = self.repo[file.linkrev()]
        date = '%d %d' % _parsedate(linkrev.date())
        description = linkrev.description()
      format = '%s %i %s %s\n'
      if self.transport:
        format = 'f%s\n%i %s %s\0'
      self.writer.write( format % (file.path(), file.size(), date, description) )

  def print_sub_repository(self, path, subrepo):
    if self.shouldPrintResult():
      format = '%s/ %s %s\n'
      if self.transport:
        format = 's%s/\n%s %s\0'
      self.writer.write( format % (path, subrepo.revision, subrepo.url))

  def visit(self, file):
    if file.sub_repository:
      self.print_sub_repository(file.path, file.sub_repository)
    elif file.directory:
      self.print_directory(file.path)
    else:
      self.print_file(file.path)

  def shouldPrintResult(self):
    return self.offset < self.result_count <= self.limit + self.offset

  def isTruncated(self):
    return self.result_count > self.limit + self.offset

  def finish(self):
    if self.isTruncated():
      if self.transport:
        self.writer.write( "t")
      else:
        self.writer.write("truncated")

class File_Viewer:
  def __init__(self, revCtx, visitor):
    self.revCtx = revCtx
    self.visitor = visitor
    self.sub_repositories = {}
    self.recursive = False

  def remove_ending_slash(self, path):
    if path.endswith("/"):
        return path[:-1]
    return path

  def view(self, path = ""):
    manifest = self.revCtx.manifest()
    if len(path) > 0 and path in manifest:
      self.visitor.visit(File_Object(False, path))
    else:
      p = self.remove_ending_slash(path)

      collector = File_Collector(self.recursive)
      walker = File_Walker(self.sub_repositories, self.visitor)

      self.visitor.visit(File_Object(True, p))
      collector.collect(manifest, p)
      collector.collect(self.sub_repositories.keys(), p, True)
      walker.walk(collector.structure, p)

@command('fileview', [
    ('r', 'revision', 'tip', 'revision to print'),
    ('p', 'path', '', 'path to print'),
    ('c', 'recursive', False, 'browse repository recursive'),
    ('d', 'disableLastCommit', False, 'disables last commit description and date'),
    ('s', 'disableSubRepositoryDetection', False, 'disables detection of sub repositories'),
    ('t', 'transport', False, 'format the output for command server'),
    ('l', 'limit', 100, 'limit the number of results'),
    ('o', 'offset', 0, 'proceed from the given result number (zero based)'),
  ])
def fileview(ui, repo, **opts):
  revCtx = scmutil.revsingle(repo, opts["revision"])
  subrepos = {}
  if not opts["disableSubRepositoryDetection"]:
    subrepos = collect_sub_repositories(revCtx)
  writer = Writer(ui)
  printer = File_Printer(writer, repo, revCtx, opts["disableLastCommit"], opts["transport"], opts["limit"], opts["offset"])
  viewer = File_Viewer(revCtx, printer)
  viewer.recursive = opts["recursive"]
  viewer.sub_repositories = subrepos
  viewer.view(opts["path"])
  printer.finish()
