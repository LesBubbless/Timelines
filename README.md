# Timelines
The Timelines viewer application is a tool for parallel visualisation of events. Primarily designed as a tool for History class.

## Controls
```
MAIN WINDOW
	
	MOUSE
	
	scroll		vertical scroll
	SHIFT+scroll	horizontal scroll
	CTRL+scroll	zoom in/out
	right-click	expand Event / Timeline
	
	KEYBOARD
	
	I		zoom in
	O		zoom out
	M		access Menu
		
MENU
	
	MOUSE			
	
	scroll		vertical scroll
	left-click	toggle Element visibility
	left-click	mark Elements for deletion (delete mode)
	right-click	opens edit mode for selected Element
		
	KEYBOARD
	
	X		exit to menu
	X (add mode)	exit add mode
	X (delete mode)	exit del mode
	X (edit mode)	exit edit mode
	T		add Timeline window
	T (add mode)	adds Timeline
	T (edit mode)	edits Timeline
	E		add Event window
	E (add mode)	adds Event
	E (edit mode)	edits Timeline	
	S		save
	D		delete mode
	D (delete mode)	deletes Elements
```

## Data Structure
```
data.txt
	all data stored here
	this file must be provided
	
dataBackup.txt
	is created on saving
	contains copy of previous data.txt
	gets rewritten on every save
		
STRUCTURE
	{} -- the program can read this
	
	{[timeline]}					reads the next line, all other text on this line is ignored
	name {any amount of words in one line}		everything but the first word is read
		
	{[event]} 					reads lines until [/event], all other text on this line is ignored
	name {any amount of words in one line} 		everything but the first word is read
	startDate {three words here}			words 2, 3, 4 get read
	endDate {three words here}
	visibility {either 0 or 1}			reads second word, expects 0 or 1
	{any amount of lines here 			notes begin on this line
	will be read}
	{[/event]}					notes end on this line, line not included
```

## Version
Stable version: 1.0

### Features under development
- GUI improvements
