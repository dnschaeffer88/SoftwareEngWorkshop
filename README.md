# SoftwareEngWorkshop
Inventory management app

README:

General Information:

PyroTask is an inventory management system prototype designed for Boeing to help Boeing manage items and classify types of storage. PyroTask is capable of various tasks that can make the item storage process more organizable. Through PyroTask, users are able to create new accounts, add new items to the storage system, manage items in the storage system, manage existing accounts, etc.

More About the Usage Information:

In the login interface, not only can existing users login into PyroTask with their usernames and passwords, but new users can also be created with ease. New users are asked to provide their names, choose their user types, set up the passwords, type in their email addresses, as well as select their corresponding roles and departments. Each user belongs to one specific department. Besides normal users who can store items into storage system, there are two other types of users. The department administrator can manage the users in a specific department and all other department-specific work. In addition, there is the “super user” who has the highest privilege and can gain access to all the information of all the departments. The “super user” also has the power to add and remove departments.

After logging in, users can view the dashboard interface, which displays all the bucket information. In the dashboard, each bucket displays its own geographic location and the current storage status of the bucket. Users are then able to select the specific bucket and click in to view the specific items that are stored in that bucket. Each item that the users want to add has its own serial number and weight, making sure that items are uniquely labeled and the buckets will not exceed their maximum storage capacity. Besides serial numbers, there is also an identifier called the “part number”. The existence of part number is to help classify the items into specific categories. Through serial numbers and part numbers, items can be uniquely identified in this inventory management system.

How this prototype is built?

The web-based prototype is built based on the combination of frontend, backend and data management system. The frontend is developed based on REACT framework, the backend is developed using Spring Boot, and Firebase is chosen as the database.
