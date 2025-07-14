#  Android Task Management App Suite
This project consists of two Android applications:

* **Manager App**: For assigning and tracking employee tasks.

* **Employee App**: For employees to view and update their assigned tasks.

* Both applications are based on a shared module that provides core logic, shared data models, adapters, and export utilities.

* Additionally, the project includes two reusable libraries:

* **fancyviews**

* **pdflibrary**

---

## Project Structure
```graphql
project-root/
├── managerapp/        # Manager-facing app
├── employeeapp/       # Employee-facing app
├── common/            # Shared module (models, adapters, export logic)
├── fancyviews/        # Custom UI components (e.g., buttons, progress bar)
└── pdflibrary/        # PDF export utility library
```
##  Shared Module (common)
The common module contains all shared logic across both apps:

### Features

* **Task** – Shared task model

* **TaskAdapter** – Common RecyclerView.Adapter used by both apps

* **BaseActivity** – Abstract activity with logging and toast helpers

* **TaskExportUtils** – Utility for exporting tasks to PDF (as tables or visual cards)

* **Gradle Configuration** – Use `implementation project(':common')` in your app's `build.gradle`
* ### Usage
* To use the shared module, include it in your app's `build.gradle` file:
```groovy
dependencies {
    implementation project(':common')
}
```
* You can then access shared classes like `Task`, `TaskAdapter`, and `TaskExportUtils` in your activities or fragments.
* ### Example
```java
import com.example.common.Task; 
import com.example.common.TaskAdapter;
import com.example.common.TaskExportUtils;
import com.example.common.BaseActivity;
public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example usage of Task and TaskAdapter
        Task task = new Task("Task 1", "Description of task 1");
        TaskAdapter adapter = new TaskAdapter(this, taskList);
        
        // Export tasks to PDF
        TaskExportUtils.exportTasksAsTable(this, taskList, uri);
    }
}
```
---

## Manager App
The Manager App allows managers to assign and track tasks for employees.
### Features
* **Task Assignment** – Create and assign tasks to employees
* **Task Tracking** – View task status
* **Task Export** – Uses `pdflibrary` for exporting tasks to PDF in table or card format
* **UI** – Custom views for task management (using `fancyviews` library)
* **Dependencies** – Depends on `common`, `fancyviews`, and `pdflibrary` modules
* **Gradle Configuration** – Uses `implementation project(':common')`, `implementation project(':fancyviews')`, and `implementation project(':pdflibrary')` in `build.gradle`
### Usage
Managers can create tasks, assign them to employees, and track their progress. The app provides a user-friendly interface for managing tasks and exporting them as needed.

---

## Employee App
The Employee App allows employees to view and update their assigned tasks.
### Features
* **Task Viewing** – View tasks assigned by managers
* **Task Updates** – Update task status 
* **Task Export** – Uses `pdflibrary` for exporting tasks to PDF in table or card format
* **Dependencies** – Depends on `common`, `fancyviews`, and `pdflibrary` modules
* **Gradle Configuration** – Uses `implementation project(':common')`, `implementation project(':fancyviews')`, and `implementation project(':pdflibrary')` in `build.gradle`
* ### Usage
* Employees can view their tasks, update their status, and export task lists as needed. The app provides a simple interface for managing personal tasks.

---

## Libraries
Both libraries (`fancyviews` and `pdflibrary`) are available via **JitPack**:
### How to Include the Libraries (via JitPack)
To include the libraries in your project, add the following to your `build.gradle` file:
```groovy
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Then, add the dependencies for each library in your app's `build.gradle` file:
```groovy
dependencies {
    implementation 'com.github.yahavLer:AndB-FinalProject:FancyViews:1.0.1'
    implementation 'com.github.yahavLer:AndB-FinalProject:PdfLibrary:1.0.1'
}
```
### fancyviews
The `fancyviews` library provides custom UI components for both apps, such as:
* **Custom Buttons** – Styled buttons with animations
* **Progress Bar** – Custom progress bar with animations
* **Custom Dialogs** – Enhanced dialog components
* **Gradle Configuration** – Use `implementation 'com.github.yahavLer:AndB-FinalProject:FancyViews:1.0.1'
` in your app's `build.gradle`
* ### Usage
* To use the custom views, simply include them in your layout XML files or instantiate them programmatically in your activities/fragments.
* ### Example
```xml
<com.example.fancyviews.CustomButton
    android:id="@+id/my_custom_button"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Click Me!" />
```
```java
import com.example.fancyviews.CustomButton;
public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CustomButton myButton = findViewById(R.id.my_custom_button);
        myButton.setOnClickListener(v -> {
            // Handle button click
        });
    }
}
```

### pdflibrary
The `pdflibrary` provides utilities for exporting task data to PDF format.
* **PDF Export** – Export tasks as tables or visual cards
* **PdfRow** – Flexible key-value structure for tabular data
* **Gradle Configuration** – Use `implementation 'com.github.yahavLer:AndB-FinalProject:PdfLibrary:1.0.1'
` in your app's `build.gradle`
* ### Usage
* To export tasks to PDF, use the `TaskExportUtils` class from the `common` module:
```java
    import com.example.common.TaskExportUtils;
    TaskExportUtils.exportTasksAsTable(context, taskList, uri);
    TaskExportUtils.exportTasksAsCards(context, recyclerView, uri);
```
* ### Example
```java
import com.example.pdflibrary.PdfRow;
import com.example.pdflibrary.PdfExportUtils;
public class PdfExportActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_export);

        // Example usage of PDF export
        List<PdfRow> pdfRows = new ArrayList<>();
        pdfRows.add(new PdfRow("Task 1", "Description of task 1"));
        pdfRows.add(new PdfRow("Task 2", "Description of task 2"));

        Uri uri = ...; // Define your output URI
        PdfExportUtils.exportToPdf(this, pdfRows, uri);
    }
}
```
---
## Conclusion
This project provides a comprehensive suite of Android applications for task management, leveraging shared logic and reusable libraries to streamline development. The Manager App and Employee App work together to facilitate efficient task assignment and tracking, while the `fancyviews` and `pdflibrary` modules enhance the user experience with custom UI components and PDF export capabilities.
## Getting Started
To get started with this project, clone the repository and open it in Android Studio. Make sure to sync the Gradle files to resolve dependencies. You can then run either the Manager App or Employee App on an emulator or physical device.
## Watch video
📹 [Click here to download and watch the demo video](media/taskTraking.mp4)

