sudo mongoexport --port 28888 -d twitter_mini -c selected_users  --out selected_users.json
sudo mongoexport --port 28888 -d twitter_mini -c users  --out all_users.json
sudo chmod 777 selected_users.json
sudo chmod 777 all_users.json
