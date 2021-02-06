# CipherPass

CipherPass is a basic password manager for Android built on top of the well known SqlCipher database encryption library. CipherPass is written in Kotlin, using the Android architecture components library.

The application has currently the following main features:
- The password data is stored in a SqlCipher database (AES 256bit encryption).
- An entry contains by default the following fields: username, password, url, comment.
- The user may add any number of custom defined fields to an entry.
- The authentication can be password based or biometric based.
- The entries are searchable.
- The entries can be sorted by creation date, modification date or entry name, ascending or descending.
- Clipboard timeout: after a configurable amount of time, the items copied to clipboard are automatically deleted.
- Background timeout: after a configurable amount of time spent by the app in the background, the user will be asked to authenticate again.
- The entries can be exported to a file and can be imported from a file. (This feature is useful for backup or for moving to another device.)