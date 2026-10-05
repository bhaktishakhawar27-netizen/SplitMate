# SplitMate - Updated Version

## Main updates
- Monthly Data now shows a 12-month bar graph of spending.
- Current month summary shows:
  - Total Budget
  - Total Expense / Spent
  - Extra Budget Added
  - Money Received
  - Remaining / Over Budget
- Added `ADD EXTRA BUDGET` for adding one-off money to the current month's budget.
- Extra budget is stored separately so the original monthly budget is not overwritten.
- Split-bill settlement flow now distinguishes between:
  - **Paid by you** -> the full bill is recorded as money spent immediately.
  - **Receive** -> money is added to available balance only after you tap `Received`.
  - **Pay** -> your share is deducted only after you tap `Paid`.
- Added a To-Do List with add, complete/uncomplete, and delete actions.
- Added a common three-dot menu to post-login screens for navigation.
- Added To-Do List to the Home navigation drawer.
- Added a clean, narrow scrollbar to scrollable pages.
- Database upgrades are now non-destructive for the new version; existing data is preserved when upgrading from the previous database version.

## Important
Open the `splitmate_updated` folder in Android Studio and let Gradle sync.
The environment used to prepare this package could not download the Gradle 9.5 distribution, so a final APK build was not performed here.

## Split Bill update
- Menu (three-dot / hamburger) is now at the TOP-LEFT on every post-login page.
- Split Bill has an **Exclude Me** checkbox: your share is not deducted from your balance.
- Each person has a **Paid / Not Paid** checkbox; ticking/unticking updates your balance automatically (reversible).
- Split Bill and Split Bill History clearly show **Money I need to RECEIVE** and **Money I need to PAY**.
- Database version 10: adds one column (`entry_id`) to `settlements`; existing data is preserved.
