# A mongo 2.67 server is set up locally in each case, avoiding the use of the default port
# Running in without user credential authentication, as it's an unnecessary step in our use case.
sudo mongod --port 28888 --noauth
