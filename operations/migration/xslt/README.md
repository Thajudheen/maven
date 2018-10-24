XSLT templates for migrating owl files
======================================

Example (migrate `security-implementation.owl` from 1508 to 1511 and format the result using `xmlstarlet`):

`xmlstarlet tr 1508_to_1511__security-implementation.xslt security-implementation.owl | xmlstarlet fo`

