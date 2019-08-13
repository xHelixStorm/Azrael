# Azrael
Discord Bot for moderation purposes with entertainment features

### Config.ini
choose commands that you want to enable, implement links to thumbnails for various features, include your user id to show that you are the admin of the bot, insert your secret token after you have created a new Bot user, insert your login credentials for the mysql databases, include your Pastebin account and more!

### Register the server
- Let the bot know the hierarchy of your Discord server by using the register command. Set which role belongs to an admin, which to a moderator, to a normal community member and to the bots.
- Register a role for an automated mute system.
- Register roles with a self defined unlock level for the ranking system.
- Register channels to decide which channel all events should be logged, which is the trash channel for removed messages by an administrator, moderator or the bot itself and enable a default bad word filter by assigning a language.

### Set or change settings
- Set one or more languages to a channel to allow various filters to check the channel once a message has been submitted
- Set a number of warnings a user is allowed to get warned before getting banned by receiving the registered mute role. While registering, you can decide how long each mute shall last.
- Disable or enable the self designed ranking system with skins you can set by your own choice and what allows you to use various related entertainment commands.
- Set an experience limit for each day in case the community should go overboard with an automated reminder in private message, for when the limit has been reached.
- Set the cap level that a user can reach
- Choose your own daily items and more...

### User command
- Utilize the user command to display various information of a user such as current warnings, all name changes and nicknames and critical events.
- Decide to change his current warning value without editing the database directly, to mute, to kick or to ban with or without giving a self chosen reason.
- Mute a user with the command to decide, if you want to choose a different not registered timer a user should be muted without adding up the warnings (Except if it's the first time the user is being muted).
- Or decide to be benevolent and gift experience points or to set a player on a self chosen level. 

### Filter command
Decide to go through three different filter lists to take actions or changes where elsewhise a direct database edit would have been necessary.
Actions can be taken for the
- word-filter: List with words that get filtered on text channels grouped by languages.
- name-filter: List with words with which names gonna be compared every time a user changes the own Discord name (not server based nickname) or joins a server. If triggered, the user will receive a name from the funny-names list.
- funny-names: List with names that a user will receive when the a word from the name-filter list is included in the name.

After choosing a list, four different actions can be taken. To display the current list by uploading it on pastebin as unlisted with a 24 hours expiration date, to insert or remove a word/name and to load all words or names from a txt file into the database. While loading from the file, all old entries will be removed.

### Ranking system
Create and bind roles with the ranking system and combine it with levels to unlock them. The highest unlocked role will be automatically assigned and can be used to give more privileges to those that have unlocked an higher role (as example). Use the profile and rank command to display your current level and experience points needed to rank up. Use the shop to purchase different skins and items and see them in your inventory. 

### Pugs and Cats
Use the dedicated commands to bring up pictures of pugs or cats into the chat.

### Requirements
- Java Runtime Environment 12 required
- At least 2gb free ram recommended for long up times and busy servers (minimum 1gb)
- Runnin MySQL Database
