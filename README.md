Deployment & Execution:
To run the system, the infrastructure must be initialized first. The startup sequence is as follows:

Start the Infrastructure API(https://github.com/hiiico/Infrastructure): This bootstraps the underlying platform, including the database, service discovery, and message bus.

Run the Notifications Microservice(https://github.com/hiiico/vacation-planning-notifications): Once the infrastructure is live, start the notifications service so it can register itself and begin listening for alerts.

Launch the Core Management App(https://github.com/hiiico/vacation_planning): Finally, start the main application, which will connect to the infrastructure and utilize the now-available notification services.
<img width="1321" height="214" alt="Microservice" src="https://github.com/user-attachments/assets/1742a54d-e705-41f7-b439-4c1814f2cbf5" />
