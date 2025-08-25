# Kalendar Widget - Upcoming events and tasks widget for Android

Kalendar is a home screen widget for your Android device. Each widget has its own settings and displays configured list of calendar events and tasks so that you can easily have a glimpse at your appointments.

<a href="https://play.google.com/store/apps/details?id=com.github.ekalin.kalendar">
<img alt="Get it on Google Play" 
    src="https://play.google.com/intl/en_us/badges/images/apps/en-play-badge.png" 
    width="150px"/>
</a>
<br/><br/>

![Kalendar Widget screenshot](assets/screenshots/widget.png)
![Preferences screenshot](assets/screenshots/prefs.png)

## Features

* No advertising. Free and Open Source.
* Displays events from selected calendars and task lists.
* Displays birthdays from you contacts.
* Select how far ahead to display events (one week, one month, etc.). Optionally shows past events.
* Automatically updates when you add/delete/modify an event. Or you may update the list instantly.
* Customize colors and text size of the widget.
* Fully resizable widget with two alternative layouts and layout customizations.
* Lock time zone when travelling to different time zones.
* Backup and restore settings, cloning widgets on the same or different devices.
* Android 6+ supported. Supports Android tablets.

Note on Tasks support: As there is no unified way to access tasks created in different applications, each application needs its own implementation. Currently supported:
* [OpenTasks (by dmfs GmbH)](https://github.com/dmfs/opentasks).
* [Tasks.org (by Alex Baker)](https://play.google.com/store/apps/details?id=org.tasks).
* Tasks of Samsung Calendar.

Tasks from Google Tasks are supported with the use
of [Tasks.org (by Alex Baker)](https://play.google.com/store/apps/details?id=org.tasks).

## Collaborate

We are developing this application in public to bring you a tool that _you_ want to use. Please feel free to
open [issues](https://github.com/ekalin/kalendar/issues) and
provide [pull requests](https://github.com/ekalin/kalendar/pulls).

## Changelog

### v10

* Support for Android 16.

### v9

* Support for Android 15.

### v8

* It is now possible to configure the background color of the header, and to add a separator betweeen the header and the
  contents.
* It is now possible to use a different language than the rest of the system (when running on Android 13 or
  later). [Change app language](https://support.google.com/android/answer/12395118?hl=en).
* Fixed crash if contacts permission (necessary in order to show birthdays) is removed.
* Support for Android 14.
* Android 6+ is now required.

### v7

* Support for Android 13.

### v6

* Adds a periodic refresh every hour, because sometimes notifications of changes in the events are missed.

### v5

* Support for Android 12.

### v4

* It is now possible to display birthdays from your contacts, if your calendar does not create events for birthdays.

### v3

* Added support for [Tasks.org (by Alex Baker)](https://play.google.com/store/apps/details?id=org.tasks). This
  application also supports Google Tasks, so you can now see your Google Tasks in Kalendar.
* Canceled events and tasks are not shown anymore.
* If the task app is not installed, a button is displayed to install it.
* Dark mode support.

### v2.4.0

* Ongoing events are highlighted in a different color.
* [Text size scaling improved](https://github.com/plusonelabs/calendar-widget/issues/301) - Now size of text in all parts of the widget changes, when you change "Text size" option.
* [Backup and Restore widget settings](https://github.com/plusonelabs/calendar-widget/issues/330). Convenient for cloning settings to another widget on the same device. Good for moving widget settings to another device, but may require calendars selection adjustment in this case.
* It's now possible to set the colors of the widget texts individually, instead of selecting a theme (which was confusingly called a "shading").
* Hopefully fixed intermittent "No events" display instead of actual list of events. Separate "[ToDo Agenda Not Initialized yet...](https://github.com/plusonelabs/calendar-widget/issues/318)" layout added to see the widget, when it is not initialized yet (e.g. after device reboot).

### v2.3.0

* [Don't show time for All day events](https://github.com/plusonelabs/calendar-widget/issues/236).
* Show end date for multi-day all day events when "Fill all day events" is disabled.
* Show end time for events spanning more than one day (but not all day events) when "Fill all day events" is disabled.
* [Display location independently of time](https://github.com/plusonelabs/calendar-widget/issues/221), including showing location for All day events.

### v2.2.0

* Fixes all-day events disappearing before the end of the day.

### [v2.1](https://github.com/plusonelabs/calendar-widget/issues/308) Tasks support

* Tasks support added. Two Task apps are supported: [OpenTasks (by dmfs GmbH)](https://github.com/dmfs/opentasks) and Tasks of Samsung Calendar. 

### [v2.0.0](https://github.com/plusonelabs/calendar-widget/issues/291) Renamed and republished

* The "Calendar Widget" renamed to "Todo Agenda" and published to Google Play as [the new application](https://play.google.com/store/apps/details?id=org.andstatus.todoagenda).

### [v1.10.1](https://github.com/plusonelabs/calendar-widget/issues?q=milestone%3A1.10.1)

* Fix issue where widget would crash because of unexpected widgetId
* Translations updates

### [v1.10.0](https://github.com/plusonelabs/calendar-widget/issues?q=milestone%3A1.10.0)

* Added: Different settings for different Widget instances allow you to create any number of
customized views of your events, including configuration of different calendars for different widgets.
All setting are separate for each Widget instance. In order not to get confused in 
configurations of different widgets, you can launch "Calendar Widget" with your Android Launcher 
and you will be presented with a widget selection list. Each widget can be given its name 
to ease configuring. See [screenshots](https://github.com/plusonelabs/calendar-widget/issues/37#issuecomment-290968481). 
* Added: "Show day headers" option to disable day headers completely.
* Added: The alternative widget layout: "All in one row". Each event can now occupy literally one line 
only. "Appearance -> Event entry layout" option allows 
to switch between the "Time below title" layout and this new one. When used with the
"Show day headers" option, this layout almost doubles number of events visible in a widget without scrolling.
"Days from Today" column replaces day headers in a case the Day Headers are 
turned off. See [screenshots](https://github.com/plusonelabs/calendar-widget/issues/42#issuecomment-289261236).
* Added: "Abbreviate dates" option allows to have shorter date format for both Widget and Day headers.
* Added: "Lock time zone" option to show events in the same Time Zone when travelling to different time zones.
* Added: "Show only the closest instance of a recurring event". You can set 
"Event Filters"->"Date range" to "One year", and your agenda won't be an endless list of the same events.
* Improved: Use space or commas in "Hide based on keywords in a title". Place a text in single or
double quotes in order to have space or comma in a filter.
* Android 7 compatibility, including support of permissions introduced in Android 6.

### [v1.9.3](https://github.com/plusonelabs/calendar-widget/issues?q=is%3Aissue+milestone%3A1.9.3)

* Date format "auto" now makes use of system wide date settings
* Bugfixes for the 1.9 release
* Translation updates

### [v1.9.2](https://github.com/plusonelabs/calendar-widget/issues?q=is%3Aissue+milestone%3A1.9.2)

* Bugfixes for the 1.9 release
* Translation updates

### [v1.9.1](https://github.com/plusonelabs/calendar-widget/issues?q=is%3Aissue+milestone%3A1.9.1)

* Bugfixes for 1.9 release

### [v1.9.0](https://github.com/plusonelabs/calendar-widget/issues?q=is%3Aissue+milestone%3A1.9)

* New preferences to filter the events shown by the widget
  * New "Show events that ended recently" option allows to show events, which ended several hours ago, today or yesterday. [#122](https://github.com/plusonelabs/calendar-widget/issues/122)
  * New "Show all past events having default color" option shows all past events, which have 'Default color'. This option may be used to treat calendar events as Tasks and mark their completion by changing event's color. [#138](https://github.com/plusonelabs/calendar-widget/issues/138)
  * New "Hide based on keywords in a title" option, which acts on all events. Multiple keywords may be separated by spaces or commas.
* Add "Past events background color" option.
* Add "Today" option to "Date range", so you can see current and future events for today only (The same "Today" option exists for past events also) [#156](https://github.com/plusonelabs/calendar-widget/issues/156)
* Add "Refresh" button to widget header to refresh the list of events [#120](https://github.com/plusonelabs/calendar-widget/issues/120)
* Add "Show days without events" option

### [v1.8.6](https://github.com/plusonelabs/calendar-widget/issues?q=milestone%3A1.8.6+is%3Aclosed)

* Add styling for Android 5.0 (Lollipop)
* Adding widget to home screen instantly adds it without showing settings ui
* Add launcher entry to more easily access the settings of the widget
* Add support for new languages swedish, ukraine, vietnamese, traditional chinese and finish (thanks to all contributors)

### [v1.8.5](https://github.com/plusonelabs/calendar-widget/issues?milestone=24&state=closed)

* Fixes problem where all day events would be shown one day off

### [v1.8.4](https://github.com/plusonelabs/calendar-widget/issues?milestone=23&state=closed)

* The calendar selection preferences now show the account a calendar comes from (thanks @thknepper)
* Fixes issue where the date shown in the widget and in a calendar app would deviate (thanks @schmaller)
* Adds translation for Portuguese, Romanian and Greek
* Updates several other other translations

### [v1.8.3](https://github.com/plusonelabs/calendar-widget/issues?milestone=22&state=closed)

* Adds Korean translation
* Fixes problem where the Norwegian translation was not visible to users

### [v1.8.2](https://github.com/plusonelabs/calendar-widget/issues?milestone=21&state=closed)

* Updates translations and adds support for simplified Chinese, Bulgarian and Norwegian

### [v1.8.1](https://github.com/plusonelabs/calendar-widget/issues?milestone=20&state=closed)

* Fixes issue where the date headers for "Today" and "Tomorrow" where not show correctly
* Updates translations and adds partial support for Hebrew (thanks bomba6)

### [v1.8](https://github.com/plusonelabs/calendar-widget/issues?milestone=19&state=closed)

* Adds option to align the date header left, right and center
* Allows to show events spreading over multiple days only on the first day
* Enables to show only events in a one day date range (thanks jganetsk)
* Adds new translations in Dutch and polish
* A big thanks to at all the people who have helped with translations on crowdin.net
 * blancazert, deamn, emes2, gabrielemariotti, hermajan, jagoda1-72, k.schasfoort, moritzpost

### [v1.7.2](https://github.com/plusonelabs/calendar-widget/issues?milestone=18&state=closed)

* Fine tunes the shading of the text colors
* Fixes tinting issue of background color

### [v1.7.1](https://github.com/plusonelabs/calendar-widget/issues?milestone=17&state=closed)

* Fixes critical bug on Android 4.0 devices
* Completes Czech translation (thanks hermajan)

### [v1.7](https://github.com/plusonelabs/calendar-widget/issues?milestone=16&state=closed)

* Adds theming capabilities to the widget background and texts
* Taping on current date header now opens calendar app
* Calendar events can now show their event specific color

### [v1.6.4](https://github.com/plusonelabs/calendar-widget/issues?milestone=15&state=closed)

* Updates Czech translation
* Fixes critical bug on Android 4.4

### [v1.6.3](https://github.com/plusonelabs/calendar-widget/issues?milestone=14&state=closed)

* Adds support for multiple languages: Spanish, French, Italian and Brazilian Portuguese
* Adds option to hide end date (contributed by ultraq)
* Background transparency is set in 5% increments instead of 10% (contributed by ultraq)
* Fixed bug where events starting or ending at midnight would not show their time correctly

### [v1.6.2](https://github.com/plusonelabs/calendar-widget/issues?milestone=13&state=closed)

* Fixed serious crash when preferences from older installations were present

### [v1.6.1](https://github.com/plusonelabs/calendar-widget/issues?milestone=12&state=closed)

* Fixed a crash of Samsungs S Planner when creating new event

### [v1.6](https://github.com/plusonelabs/calendar-widget/issues?milestone=11&state=closed)

* Added option to show the event location
* Added option to span title over multiple lines
* Added ability to choose from more font sizes
* Added option to set the event date range

### [v1.5.2](https://github.com/plusonelabs/calendar-widget/issues?milestone=10&state=closed)

* Added support for Hungarian and Russian language

### [v1.5.1](https://github.com/plusonelabs/calendar-widget/issues?milestone=9&state=closed)

* Added support for Czech language
* Fixed crashes when adding event on certain devices
* Fixed issue with spanning events

### [v1.5](https://github.com/plusonelabs/calendar-widget/issues?milestone=8&state=closed)

* Enabled support to place the calendar widget on the lock screen
* Calendar events can now be added directly from the widget
* Added Japanese translations (Thanks to Sakuma)
* Fixed several stability issues

### [v1.4](https://github.com/plusonelabs/calendar-widget/issues?milestone=7&state=closed)

* Fixed critical issue where an all-day event would be displayed one day to early in certain timezones
* Added ability to configure the transparency of the widget background

### [v1.3.2](https://github.com/plusonelabs/calendar-widget/issues?milestone=6&state=closed)

* Added support for custom event colors
* Calendar colors are now correctly shown on Jelly Bean
* Fixed issue when opening the calendar selection preference activity
* Fixed problem with events that span multiple days

### [v1.3.1](https://github.com/plusonelabs/calendar-widget/issues?milestone=5&state=closed)

* Fixed problem where noon was display as 0:00 pm instead of 12:00 pm
* Fixed issue when no calendar is present on the device

### [v1.3](https://github.com/plusonelabs/calendar-widget/issues?milestone=4&state=closed)

* Added support for the am/pm date format
* Events that span multiple days now create multiple entries in the widget
* Touching a calendar entry now provides visual touch feedback
* Events without title now indicate that there is no title

### [v1.2.1](https://github.com/plusonelabs/calendar-widget/issues?milestone=3&state=closed)

* Fixed issue where the date in the calendar app would be displayed wrong when opened from the widget

### [v1.2](https://github.com/plusonelabs/calendar-widget/issues?milestone=2&state=closed)

* Added indicators for events with an alert
* Added indicators for recurring events
* Widget refreshes at midnight
* Widget handles time, date, timezone and locale changes correctly

### [v1.1](https://github.com/plusonelabs/calendar-widget/issues?milestone=1&state=closed)

* Added preferences menu
* Added option to select the active calendars for the widget
* An new preferences option allows to hide the current date header to have more space for the calendar entries
* The text size can not be customized as small, medium or large
* The widget can not be shrinked properly on all devices

### v1.0

* Initial Release


## Licence and authors

Kalendar Widget is released under the [Apache 2.0 Licence](LICENSE).

Kalendar Widget is currently maintained by Eduardo M KALINOWSKI. It is based on [ToDo Agenda](https://github.com/plusonelabs/calendar-widget), written by Yuri Volkov and Moritz Post, with help from other contributors and translators.
