# console.blog
a tech blog you can access from your terminal

# instructions for running the application:
clone the repository
set up the .env file in the root directory (contact ryan.c.nguyen@sjsu.edu or ryan.shim@sjsu.edu)
give ./cblog.sh executable permission
run mvn clean package or ./cblog.sh -b to build the application
- CLI:
  - run ./cblog.sh to access the application
  - use help command to see the list of available commands
- Website
  - in one terminal, run ./cblog.sh -ws to start the web server
  - cd into front end, run npm install to download the dependencies
  - run npm start to start up the front end development server
  - access the application from your browser
