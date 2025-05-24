# 2025-SE-Group-15 Project Description

This is the project of our group in the 2025 Software Engineering course, aiming to implement a Personal Finance Management System. 

## Project Structure

```
2025-SE-Group-15/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── personalfinance/
│                   ├── MainSystem.java
│                   ├── controller/
│                   ├── model/
│                   ├── storage/
│                   └── view/
├── avatars/
├── component/
├── data/
├── target/            
├── Javadoc/                        
│   └── index.html      
├── manual/                     
│   └── user_manual.pdf    
├── test/       
├── LICENSE
├── pom.xml   
└── README.md
```

## Features Overview

The system allows users to:

* Record and categorize income and expenses
* Generate financial reports and charts
* Set budgets and reminders
* Persist data locally (using files)

## Usage Instructions

### Clone the repository

```bash
git clone https://github.com/sh2022213/2025-SE-Group-15.git
cd 2025-SE-Group-15
```

### Compile and run

1. Make sure Java 17 or higher is installed.
2. Use the following commands to compile and run the project:

```bash
javac src/main/java/com/personalfinance/MainSystem.java
java com.personalfinance.MainSystem
```

On the first run, the system will prompt you to set up initial configurations.

### Configuration files

The system stores user settings in the `data` folder. You can find this folder in the project root directory and edit the files as needed.

## Individual contribution

**QM no: 221165452 – Xiangxuan Feng**
 As team leader, implemented the transaction core via stateful `FinanceController` and bidirectional binding between the `Transaction` model and Swing UI. Built a strategy-based, format-agnostic `TxtFileParser` with regex and JSR-310 support. Standardized charts using JFreeChart factories across panels. Enforced GitHub branch protections and resolved major UI/backend merge conflicts.


**QM no: 221165315 – Jianzhang Guo**
 Architected authentication and UI navigation. Developed `LoginDialog` with dual-mode layout, validation chains, and `MainFrame` with state-aware tab switching. Engineered user profile modals, session control, logout/restart flow, and background data loading for responsiveness.


**QM no: 221165290 – Yutong Liu**
 Designed financial analysis modules including `AIAnalyzer` for NLP-based category prediction, anomaly detection, and budget recommendation. Handled CRUD operations and authentication in `FinanceController`. Built modular file parsers (e.g., `TxtFileParser`, `XlsFileParser`) for extensible format support.


**QM no: 221165393 – Haitong Lan**
 Developed key Swing UI components: `CategoryComboBox` for editable dropdowns, `CurrencyTextField` for accurate monetary input using `BigDecimal`, and `MyDatePicker` with a responsive calendar, day highlighting, and real-time synchronization via `MyMonthView`.


**QM no: 221164857 – Heyu Ren**
 Created core domain models: `Transaction`, `User`, and `Budget`, enabling precise financial logic. Enforced type constraints, secured user data with hashing, and used builder patterns for flexibility. Emphasized encapsulation and modular utility functions for core operations.


**QM no: 221164776 – Qiyue Wan**
 Built a JSON-based data management system using Gson, supporting generic collection (de)serialization via `TypeToken`, user-specific directories, and safe CRUD operations. Implemented error-tolerant file handling and scalable multi-user architecture with strong modularity.



## License

This project is licensed under the MIT License. Please see the [LICENSE](LICENSE) file for details.
