# Fix Compilation Error: Missing educationAmountText

The project fails to compile because `HomeActivity.java` references several View IDs that are not defined in the corresponding layout file `activity_home.xml`. Specifically, `educationAmountText`, `entertainmentAmountText`, `billsAmountText`, and `otherAmountText` are missing.

## User Review Required

> [!IMPORTANT]
> I will add the missing category rows to the `activity_home.xml` layout to resolve the compilation errors. I will also take the liberty to add "Canteen" to the `AddExpenseActivity` categories list, as it was present in `HomeActivity` but missing from the entry form.

## Proposed Changes

### app

#### [MODIFY] [activity_home.xml](file:///C:/Users/LENONO/AndroidStudioProjects/SplitMate/app/src/main/res/layout/activity_home.xml)
- Add missing category rows for Education, Entertainment, Bills, and Other.
- Assign appropriate IDs: `educationAmountText`, `entertainmentAmountText`, `billsAmountText`, and `otherAmountText`.
- Use consistent styling (emojis and background colors) for the new categories.

#### [MODIFY] [AddExpenseActivity.java](file:///C:/Users/LENONO/AndroidStudioProjects/SplitMate/app/src/main/java/com/example/splitmate/AddExpenseActivity.java)
- Add "Canteen" to the `categories` array to match the categories displayed on the Home screen.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugJavaWithJavac` to verify the compilation error is resolved.
- Build the app to ensure all resource IDs are correctly generated.

### Manual Verification
- Deploy the app and verify that the Home screen now displays all categories: Food, Canteen, Travel, Shopping, Education, Entertainment, Bills, and Other.
- Verify that adding an expense for any of these categories updates the Home screen correctly.
