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


import os
from mercurial import demandimport, ui as uimod, hg
from mercurial.hgweb import hgweb, wsgicgi

demandimport.enable()

try:
    u = uimod.ui.load()
except AttributeError:
    # For installations earlier than Mercurial 4.1
    u = uimod.ui()

u.setconfig('web', 'push_ssl', 'false')
u.setconfig('web', 'allow_read', '*')
u.setconfig('web', 'allow_push', '*')

u.setconfig('hooks', 'changegroup.scm', 'python:scmhooks.postHook')
u.setconfig('hooks', 'pretxnchangegroup.scm', 'python:scmhooks.preHook')

# pass SCM_HTTP_POST_ARGS to enable experimental httppostargs protocol of mercurial
# SCM_HTTP_POST_ARGS is set by HgCGIServlet
# Issue 970: https://goo.gl/poascp
u.setconfig('experimental', 'httppostargs', os.environ['SCM_HTTP_POST_ARGS'])

# open repository
# SCM_REPOSITORY_PATH contains the repository path and is set by HgCGIServlet
r = hg.repository(u, os.environ['SCM_REPOSITORY_PATH'])
application = hgweb(r)
wsgicgi.launch(application)
