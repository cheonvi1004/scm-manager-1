# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased
### Added
- Add iconStyle + onClick option and story shot for icon component ([#1100](https://github.com/scm-manager/scm-manager/pull/1100))
- Making WebElements (Servlet or Filter) optional by using the `@Requires` annotation ([#1101](https://github.com/scm-manager/scm-manager/pull/1101))

### Changed
- Removed the `requires` attribute on the `@Extension` annotation and instead create a new `@Requires` annotation ([#1097](https://github.com/scm-manager/scm-manager/pull/1097))
- Use os specific locations for scm home directory ([#1109](https://github.com/scm-manager/scm-manager/pull/1109))
- Use Library/Logs/SCM-Manager on OSX for logging ([#1109](https://github.com/scm-manager/scm-manager/pull/1109))

### Fixed
- Protocol URI for git commands under windows ([#1108](https://github.com/scm-manager/scm-manager/pull/1108))

## [2.0.0-rc7] - 2020-04-09
### Added
- Fire various plugin events ([#1088](https://github.com/scm-manager/scm-manager/pull/1088))
- Display version for plugins ([#1089](https://github.com/scm-manager/scm-manager/pull/1089))

### Changed
- Simplified collapse state management of the secondary navigation ([#1086](https://github.com/scm-manager/scm-manager/pull/1086))
- Ensure same monospace font-family throughout whole SCM-Manager ([#1091](https://github.com/scm-manager/scm-manager/pull/1091))

### Fixed
- Authentication for write requests for repositories with anonymous read access ([#108](https://github.com/scm-manager/scm-manager/pull/1081))
- Submodules in git do no longer lead to a server error in the browser command ([#1093](https://github.com/scm-manager/scm-manager/pull/1093))

## [2.0.0-rc6] - 2020-03-26
### Added
- Extension point to add links to the repository cards from plug ins ([#1041](https://github.com/scm-manager/scm-manager/pull/1041))
- Libc based restart strategy for posix operating systems ([#1079](https://github.com/scm-manager/scm-manager/pull/1079))
- Simple restart strategy with System.exit ([#1079](https://github.com/scm-manager/scm-manager/pull/1079))
- Notification if restart is not supported on the underlying platform ([#1079](https://github.com/scm-manager/scm-manager/pull/1079))
- Extension point before title in repository cards ([#1080](https://github.com/scm-manager/scm-manager/pull/1080))
- Extension point after title on repository detail page ([#1080](https://github.com/scm-manager/scm-manager/pull/1080))

### Changed
- Update resteasy to version 4.5.2.Final
- Update shiro to version 1.5.2
- Use browser built-in EventSource for apiClient subscriptions
- Changeover to MIT license ([#1066](https://github.com/scm-manager/scm-manager/pull/1066))

### Removed
- EventSource Polyfill
- ClassLoader based restart logic ([#1079](https://github.com/scm-manager/scm-manager/pull/1079))

### Fixed
- Build on windows ([#1048](https://github.com/scm-manager/scm-manager/issues/1048), [#1049](https://github.com/scm-manager/scm-manager/issues/1049), [#1056](https://github.com/scm-manager/scm-manager/pull/1056))
- Show specific notification for plugin actions on plugin administration ([#1057](https://github.com/scm-manager/scm-manager/pull/1057))
- Invalid markdown could make parts of the page inaccessible ([#1077](https://github.com/scm-manager/scm-manager/pull/1077)) 

## [2.0.0-rc5] - 2020-03-12
### Added
- Added footer extension points for links and avatar
- Create OpenAPI specification during build
- Extension point entries with supplied extensionName are sorted ascending
- Possibility to configure git core config entries for jgit like core.trustfolderstat and core.supportsatomicfilecreation
- Babel-plugin-styled-components for persistent generated classnames
- By default, only 100 files will be listed in source view in one request

### Changed
- New footer design
- Update jgit to version 5.6.1.202002131546-r-scm1
- Update svnkit to version 1.10.1-scm1
- Secondary navigation collapsable

### Fixed
- Modification for mercurial repositories with enabled XSRF protection
- Does not throw NullPointerException when merge fails without normal merge conflicts
- Keep file attributes on modification
- Drop Down Component works again with translations

### Removed
- Enunciate rest documentation
- Obsolete fields in data transfer objects

## [2.0.0-rc4] - 2020-02-14
### Added
- Support for Java versions > 8
- Simple ClassLoaderLifeCycle to fix integration tests on Java > 8
- Option to use a function for default collapse state in diffs

### Changed
- Use icon only buttons for diff file controls
- Upgrade [Legman](https://github.com/sdorra/legman) to v1.6.2 in order to fix execution on Java versions > 8
- Upgrade [Lombok](https://projectlombok.org/) to version 1.18.10 in order to fix build on Java versions > 8
- Upgrade [Mockito](https://site.mockito.org/) to version 2.28.2 in order to fix tests on Java versions > 8
- Upgrade smp-maven-plugin to version 1.0.0-rc3

### Fixed
- Committer of new Git commits set to "SCM-Manager <noreply@scm-manager.org>"

## [2.0.0-rc3] - 2020-01-31
### Fixed
- Broken plugin order fixed
- MarkdownViewer in code section renders markdown properly

## [2.0.0-rc2] - 2020-01-29
### Added
- Set individual page title
- Copy on write
- A new repository can be initialized with a branch (for git and mercurial) and custom files (README.md on default)
- Plugins are validated directly after download
- Code highlighting in diffs
- Switch between rendered version and source view for Markdown files 


### Changed
- Stop fetching commits when it takes too long
- Unification of source and commits become "code"

### Fixed
- Classloader leak which caused problems when restarting
- Failing git push does not lead to an GitAPIException
- Subversion revision 0 leads to error
- Create mock subject to satisfy legman
- Multiple versions of hibernate-validator caused problems when starting from plugins
- Page title is now set correctly
- Restart after migration

## [2.0.0-rc1] - 2019-12-02
### Added
- Namespace concept and endpoints
- File history
- Global permission concept
- Completely translated into German with all the text and controls of the UI
- Frontend provides further details on corresponding errors
- Repository branch overview, detailed view and create branch functionality
- Search and filter for repos, users and groups
- Repository Permissions roles
- Migration step framework and wizard
- Plugin center integration
- Plugins can be installed (even without restart), updated and uninstalled using the new plugins overview
- Git-LFS support (with SSH authentication)
- Anonymous access via git-clone and API access with anonymous user
- Cache and x-requested-with header to bundle requests
- remove public flag from repository and migrate permissions to anonymous user

[2.0.0-rc1]: https://github.com/scm-manager/scm-manager/releases/tag/2.0.0-rc1
[2.0.0-rc2]: https://github.com/scm-manager/scm-manager/releases/tag/2.0.0-rc2
[2.0.0-rc3]: https://github.com/scm-manager/scm-manager/releases/tag/2.0.0-rc3
[2.0.0-rc4]: https://github.com/scm-manager/scm-manager/releases/tag/2.0.0-rc4
[2.0.0-rc5]: https://github.com/scm-manager/scm-manager/releases/tag/2.0.0-rc5
[2.0.0-rc6]: https://github.com/scm-manager/scm-manager/releases/tag/2.0.0-rc6
[2.0.0-rc7]: https://github.com/scm-manager/scm-manager/releases/tag/2.0.0-rc7
