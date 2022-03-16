# Changelog
All notable changes to pdf-service will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added

- Something


## [1.1.1]

Correctly packaged package


## [1.1.0]
### Added

- `services/conf/resources/fop.xconf` specifies font dir as `/home/dodpdfsv/services/conf/resources/fonts/` to prevent
  problems with relative paths

- date pattern Named month require at least one alphabetic character in month name. 
  This should prevent date parsing errors like `1961/62-1981.` as seen in Sag-1093685

- Added a lot of date parsing tests to account for all the weird and wonderful ways a Librarian can write a date

- Packaged in a TEK-compatible structure

## [1.0.0]
### Added

- Initial release of pdf-service
