## mailapageaday

A daemon written in Java that reads a document directory on user's machine once a day and mails a page of each document to the user.

### Mailer configuration

Create a file called mailer.properties with your email settings:
```
user.email: <your email>
user.name: <your username>
user.password: <your password>
mail.smtp.auth: true
mail.smtp.starttls.enable: true
mail.smtp.host: smtp.gmail.com
mail.smtp.port: 587
```
