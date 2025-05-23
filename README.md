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

## Development and Contribution

We welcome any form of contribution, including but not limited to:

* Bug fixes
* Feature suggestions
* Documentation improvements

## License

This project is licensed under the MIT License. Please see the [LICENSE](LICENSE) file for details.
