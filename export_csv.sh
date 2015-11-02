sudo mongoexport --port 28888 -d twitter_mini -c selected_users --csv --fieldFile selected_user_csv_fields.txt --out selected_users.csv
sudo mongoexport --port 28888 -d twitter_mini -c users --csv --fieldFile all_user_csv_fields.txt --out all_users.csv
sudo chmod 777 selected_users.csv
sudo chmod 777 all_users.csv
