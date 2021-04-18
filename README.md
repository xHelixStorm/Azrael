# Azrael Bot

## Server related options

When the Bot joins a server, it will automatically create a configuration file for the affected server. In it, commands and features can be either enabled or disabled. These parameter options exist:

- Administrator
one user who has all permissions to execute commands (Usually set on Heiliger#7143 for consultations and demonstrations).
- Command Prefix
commands can have a unique prefix for each server (default ‘H!’).
- Join Message
notify moderators and administrators when users join the server (a log channel is required).
- Leave Message
notify moderators and administrators when users leave the server (a log channel is required).
- Channel Log
Text channel messages can be logged into a file.
The option is now obsolete and is only used to test functionalities of the Bot.
- Cache Log
Written and edited messages are temporarily written into the cache. 
Up to 10000 messages can be cached for every server. 
Cached messages are required to print deleted messages and to print edited messages together with the original message.
- Double Experience
If used together with the ranking system, all users receive double the amount of experience points when they write in text channels. 
It can be kept active every day or between 2 days of the week (days can be chosen or disabled entirely).
- Force Reason
when a command is used to take disciplinary actions such as mute, kick or ban, then a reason has to be provided. When disabled, the user can decide, if a reason should be included or not.
- Override Ban
In a situation when a Discord user has been muted a certain amount of times to receive an automatic ban, the Bot will turn the ban into a permanent mute, keeping the user on the server..
- URL-Blacklist
When a text channel has been set to verify all urls, the Bot will automatically remove the message containing a url, unless the shared domain or subdomain of the url is listed in a whitelist. 
When disabled, all urls are allowed, even when the url verification of a text channel is enabled. In this case, urls are removed when their domain or subdomain name is registered in the URL-Blacklist.
- Self Deleted Messages
Enable the functionality to display messages that users delete on their own.
Administrators and moderators are excluded. 
The requirement is the enabled cache log functionality and either a registered trash channel or self deleted channel.
- Edited Message
When users edit their messages, the updated messages will be copied and printed into a separate text channel.
Administrators and moderators are excluded.
The requirement is the enabled cache log functionality and either a registered trash channel or edit channel.
- Edited Message History
Prints the whole message history of an edited message into a separate text channel (original message before the edit is included). 
Each consecutive edit will be printed in addition together with the total edit count. 
Edited Message functionality has to be enabled.
- Notifications
Administrators and moderators are notified when the Bot restarts, starts shuts down (disabled by default).
Required is a registered log channel.
- New Account On Join
Users with a new Discord account (created within 24 hours) will be displayed in a separate text channel with the exact time in hours and minutes of when it was created. 
Works separately than the Join Message functionality.
Required is a registered log channel.
- Reassign Roles After Mute
For cases when assigned roles are indispensable, those will be reassigned to the user after the mute timer elapses (Receiving the mute role automatically strips all removable roles from a user).
- Private & Public Patchnotes
Users can be informed when the Bot receives new patch notes (disabled by default).
- Message Deletion
When enabled, disciplinary actions such as mute, kick or ban through commands, grants the possibility to delete a chosen number of messages written by the affected user.
Can be enabled or disabled individually for mute, kick or ban.
- Force Message Deletion
Forces the user who is taking disciplinary action against a user with a command to apply a number of messages to delete. 
Can be enabled or disabled individually for mute, kick or ban.
- Auto Delete Messages
When a disciplinary action is taken, the defined number of messages will be automatically deleted from the user without asking for any consent. 
Force Message Deletion needs to be enabled for use.
Can be enabled or disabled individually for mute, kick, ban.
- Send Reason
When an administrative action is taken against a user, the applied reason will be sent in a private message to the user.
By default users receive a normal notification without reason in private messages.
Can be enabled or disabled individually for mute, kick or ban.
- Google Functionalities
Allow the use of Google spreadsheet bindings to be executed.
- Google Main Email
When an email is given and a spreadsheet is created with a command, ownership is passed to the given email and the permissions are adapted for the Bot and owner to use.


## Commands

Each command can be enabled or disabled individually. When commands are enabled, a permission level is given. For example, not every user can use the same commands. Moderation commands have a higher level than entertainment commands by default.
Administrator/Moderator commands

### Moderation commands

- Register
To register roles
adm: Administrators with the permissions to use all commands.
mod: Moderators with a higher range of commands to use.
com: Community with basic permissions to utilize entertainment commands.
key: Giveaway role to send a unique code to a user (the same user can’t receive a reward multiple times).
mut: Mute role to keep users from utilizing text channels and voice channels for a defined period of time (Depending on the settings, a user can have up to 5 warnings with a mute before receiving a ban or a permanent mute).
rea: Receive roles when reacting to specific messages (trigger reactions are to be set up with a different command).
ver: Role to assign when users have been verified (A category needs to be registered before it can be used).
To register categories
ver: Users who join a server will be put into a new text channel where only this user and administrators and/or moderators reside, waiting to be verified before they’re able to utilize all text and voice channels of the server.
To register text channels
lang: Restrict a text channel to one specific language so that the fitting language word censor list can be applied.
bot: Restrict entertainment commands to bot channels and disable the gaining of experience points in it (multiple bot channels can be registered).
com: Competitive channels which, depending on the type (co1 - co6) have different rules for the related competitive commands (e.g. Matchmaking and Clan). 
del: Text channel to log messages which have been deleted from users themselves (Option has to be enabled in the server settings too).
edi: Text channel to log edited messages (Option has to be enabled in the server settings too).
rea: When default reactions have been defined, a message with reactions will be printed on that text channel for self-serving roles.
rss: Default text channel to collect all types of subscriptions (RSS, Twitter and Reddit).
tra: Trash channel to log all kinds of removed or edited messages in case the other text channels are not defined.
log: Print server join/leave messages, error messages and other feature related messages.
upd: Notify moderators and administrators when channels or roles have been created, edited, removed into a text channel.
vot: Vote text channel which added thumbs up and thumbs down reaction on every written message (can be combined with google spreadsheets).
vo2: The same as vot with an additional shrug reaction.
To filter urls from text channels
Depending on the server configuration, it will either delete all urls by default and exclude all whitelisted domains or allow all urls and remove only blacklisted domains.
To remove only messages from text channels
When you want a text channel to be exclusive to creator content, all messages which don’t include an url or a picture, will be deleted automatically.
To register all text channels into the database (now obsolete)
To register ranking roles 
When the ranking system is enabled, roles can be registered with a level and when users reach that level, they will receive that role from the Bot.
To register all users on the server (now obsolete).
- Set
To update the permission level of a role to a higher or lower level (unlocks or restricts the usage of commands).
To add additional channel censor languages onto a text channel.
To set a maximum allowed number of warnings that a user can receive while also defining the mute time for each individual warning (minimum 1 and maximum 5 warnings in total).
To either enable or disable the ranking system (disabled by default).
To set a limit of the maximum experience points that can be gained every day (only when the ranking system is enabled).
To set the default skin for level ups, rank command, profile command and level icons. 
To define which items can be obtained from the Daily command (soon to be obsolete).
To set giveaway rewards (can be codes) to be won from the Daily command or from the giveaway role.
To define servers for competitive commands (was planned for Ironsight where players don’t match up with players from other servers while planning their own ranked game).
To set a limit of members a clan can have.
To set a limit of members a matchmaking room can have.
To set available maps for competitive commands (this is meant to be actual game related maps, which can be chosen by players before a ranked match is started).
To set the default language for the server (new users who join the server will have the same language set as the server language).
- Remove
To remove registered roles and ranking roles.
To remove registered categories.
To remove registered text channels.
To remove censoring languages from a text channel.
- User
Display information from a user such as total warnings, total bans, the first time the user has joined the server, the last time the user has joined the server and more.
Delete up to 100 messages from a user and upload to pastebin.
Change or reset the warning of a user. Warnings increase when they are muted.
Mute a user with default warning rules which increments the warning by one or by selecting a self chosen mute timer which doesn’t increase the warning value.
Lift a current running mute timer.
Ban a user and apply a reason. 
Unban a user with a reason.
Kick users and apply a reason.
Assign roles and log the name of the moderator who assigned them.
Remove roles and log the name of the moderator who removed them.
Display all individual events that the user has gone through, such as removed messages with pastebin url or banned users with the responsible moderator name and more.
Enable or disable the functionality to closer inspect users by logging either their deleted or all written messages into a separated text channel.
Gift experience points, set experience points, gift currency, set currency or set a level when the ranking system is enabled.
- Filter
Set a censoring list individually for every available language
Save words which will trigger automatic renames of users with nicknames. The same can be done to kick users when they have saved words in their names (e.g. [GM]).
Set names that a user can receive when they have to be renamed. 
Save staff names so that a warning notification will be sent into the log channel, upon using the same name.
Save urls which are not allowed to be used on a text channel (url censoring has to be enabled).
Save urls which are to be excluded from the url censoring (url censoring has to be enabled).
Save usernames from Twitter that have to be ignored while fetching tweets. 
- Write
Define a message for the Bot to write on a different channel. 
- Edit
Edit any message that the user wrote or was made to send or add reactions to optionally bind them to roles that users can receive. 
- Accept
When a verification category is registered, and a new text channel has been created with a user inside upon server join, then the command will verify the user with a role and delete the generated text channel.
- Deny
Deny the user the access to the server by kicking them out from the server, when a verification category is registered.
- Display
show a list of either all server roles, text channels, registered roles or registered text channels. 
- DoubleExperience
Enable or disable the double experience event on the Discord server (the ranking system needs to be enabled).
- Google
Create, remove or link spreadsheets to the Bot as well as assign events and map fields.
When all options and the server configuration are set, then rows can be added into the spreadsheet by the Bot (e.g. when the mute event has been assigned, a new row will be inserted with details of the muted user).
These events are available:
MUTE
Every instance a user is muted.
MUTE_READD
Every instance the user was muted, the role was removed while the timer didn’t elapse and finally the mute role was added again.
UNMUTE
Every instance the mute role is removed from a user when the timer elapses or is unmuted by command.
UNMUTE_MANUAL
Every instance when the mute role is removed manually from a user when the timer did not yet elapse.
KICK
Every instance when a user is kicked from the server.
BAN
Every instance when a user gets banned from the server.
UNBAN
Every instance when a user gets unbanned.
RENAME
Every instance when the Bot assigns or changes the nickname of a user.
RENAME_MANUAL
Every instance when a moderator or administrator manually assigns or changes the nickname of a user.
VOTE
For every written message and votes contained on a vot or vo2 channel.
EXPORT
Upload the total member count of a server on midnight MEZ.
COMMENT
Upload every message or message update (text channel restriction is required).
Mute
Separate command to mute multiple users at the same time utilizing the default warning settings.
A reason can be given as the last parameter.
- HeavyCensoring
To enable when the moderation will become hard to manage.
In addition to the language censoring, messages that contain images and unicodes or that are repeated by either themselves or copied from other users will be removed.
The command bears its own limit threshold, meaning that each time when a message is removed, the counter will increase by one. When this counter reaches the threshold, users will be muted with the default warning settings, each time a message is removed.
The threshold counter decreases automatically by one every minute or when the feature is disabled and then re-enabled. 
- Prune
To either kick all users or a specific group of users from the server.
The speed users are kicked from the server is limited by discord. Depending on the number of server members, it will take several minutes.
- Schedule
To print messages on a text channel in specific intervals like a cron job (e.g. every 5 minutes, every Thursday of the week and so on). 
- RoleReaction
To either enable or disable the functionality to receive roles by reacting on defined messages.
- Subscribe
Fetch RSS feeds or tweets from Twitter into a text channel. 
By default, the registered rss channel is used to print the fetched content. 
Each setup can be restricted to a different text channel (registering a default channel is not required when this is used).
The display format for RSS feeds and tweets can be defined. 
There are additional options for fetching content such as, fetch tweets containing only one or two selected hashtags or which contain only videos and images.
The Bot will try to fetch new content every 10 minutes.
- Reddit
Command with similar functionalities as the Subscribe command but for reddit. 
These types of contents can be fetched:
All submitted posts on a subreddit.
All submitted posts of a user.
All comments written by the user.
All upvoted comments or posts by the user (privacy settings have to be enabled).
All downvoted comments or posts by the user (privacy settings have to be enabled).
All hidden content by the user.
All saved content by the user.
All gilded content by the user.
- Warn
Write a user a warning in a private message for when they commit a rule violation.
- Shutdown
Can be used only by Heiliger#7143.
- Reboot
Can be used only by Heiliger#7143.


### Entertainment commands
- Clan
Create, manage or join clans.
- Matchmaking
Create a matchmaking room for everyone to join and to start a ranked match.
- Join
Join a created matchmaking room.
- Leave
Leave a joined matchmaking room.
- Pick
Pick a user who has entered a matchmaking room and make him join your team.
Command limited to specific matchmaking room restrictions and to room captains.
- Master
Change the room master of the current room. 
Room masters are able to start matches or restrict the member limit of the current room.
- Restrict
restrict the number of allowed users in the current matchmaking room.
- Changemap
Change the map of the current matchmaking room.
- Queue
Display all users inside the matchmaking room.
- CW
Plan clan war matches against other clans.
- Start
A room master of a matchmaking room will utilize this command to set the current room to start (users can’t join or leave once a matchmaking room status has been changed).
- Room
Command to display options for every matchmaking or clan war room as well as to choose winning teams. 
- Leaderboard
Display the current ranking in the competitive scene.
- Stats
Displays the user’s own characteristics such as the win / lose ratio, played matches, lost matches and more.
- Daily
Obtain rewards once a day (only when the ranking system is enabled).
- Equip
Equip weapons purchased from the Bot shop or that have been obtained from the Randomshop (it’s still work in progress. Weapons can’t be used, only equipped).
- Inventory
Display all purchased or obtained skins, weapons and items (ranking system needs to be enabled).
- Shop
Purchase skins, items or weapons (ranking system needs to be enabled).
- Profile
Show a screenshot of your profile page (ranking system needs to be enabled).
- Rank
Similar to Profile but shows less information.
- Use
Change the skin to use for level ups, Rank command, Profile command and level icons.
- Top
Displays the ranking of all users on the server sorted by experience points (ranking system needs to be enabled).
- Randomshop
Display obtainable weapons or pay a small fee to obtain a random weapon with random stats (limited to available S4 weapons).
- Quiz
Command to prepare and start a quiz session with questions and rewards (Note: the command was never tested live).
- Meow
Display a cat picture.
- Pug
Display a picture of a pug (dog race).

### Other commands
- Patchnotes
Display released patchnotes of the Bot. 
- About
Displays a short summary of the Bot with a redirection to the github repository.
- Help
Display all available commands.
Community members are not able to see commands meant for moderation purposes.
- Language
Change the language of the Bot on user level.

### Custom commands

Custom commands can be defined for easy requests, such as retrieving urls of songs from youtube, print a static message or receive a role when the command has been used and more.