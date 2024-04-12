# Regex explanation
In order to manage the translation for a project, we have to transform (deserialize)
the translation files into objects that we can use to display and modify the translations.
This deserialization process is done by a regex. This document will describe that grammar
that has been used in the deserialization process.

## Translation row finder
```regexp
(?:'((?:\\'|[^'])*)'|"((?:\\"|[^"])*)")\s*=>\s*(?:'((?:\\'|[^'])*)'|"((?:\\"|[^"])*)")
```
The regex above will find a translation row based on strict set of rules:
- At first, we will search for the translation key. Because we can start with either a single or
double quote we start with a non-capturing group that allows us to do the 'or' check.
The first expression in the initial capturing group checks for the key based on a single
quote:
  ```regexp
  '((?:\\'|[^'])*)'
  ```         
  Since PHP allows us to escape our own closing quote by adding a backslash in front of it e.g. `\'`, we
  have to add another non-capturing group that matches either the escaping combination or anything
  that does not escape. On top of that we capture everything inside the quotes in a group so that
  we can retrieve the key afterwards.
  <br>
  <br>
  Now that we match everything between single quotes, whilst ensuring we don't quit at the PHP
  escape combination, we also have to do the same for double quotes:
  ```regexp
  "((?:\\"|[^"])*)"
  ```
  This expression does the exact same, but it is for the double quotes string variant. Now we have
  everything required to match the key.
- Translation, however, are more than just a key. A translation is always a key-value pair. To make
  our translation pattern more strict, we continue the current regular expression. After the first key
  in an array, we always get the `=>` arrow sign to assign a value to a key. Now we can use however many
  (or zero) spaces that we want when assigning a key to a value. To match for this pattern we use the
  following expression:
  ```regexp
  \s*=>\s*
  ```
- The third and final step is matching the value. Since this is, yet again, a PHP string pattern, we can
  simply reuse the expression from the first step.