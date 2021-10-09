CREATE TABLE IF NOT EXISTS archive (id int NOT NULL AUTO_INCREMENT, ts datetime NOT NULL, temp_out float NULL, temp_in float NULL, hum_out smallint NULL, hum_in smallint NULL, barometer int NULL, rain float NULL, rain_rate float NULL, wind_avg float NULL, wind_dir smallint NULL, wind_high float NULL, solar smallint NULL, uv float NULL, PRIMARY KEY (id));

CREATE TABLE IF NOT EXISTS current (id int AUTO_INCREMENT,bar_trend smallint NULL,console_battery float NULL,forecast_icons varchar(28) NULL,forecast_msg varchar(255) NULL,sunrise datetime NULL,sunset datetime NULL,ts datetime NOT NULL,transmit_battery smallint NULL,PRIMARY KEY (id));

CREATE TABLE IF NOT EXISTS status (id int AUTO_INCREMENT,last_dl datetime NOT NULL,last_rec datetime NOT NULL,PRIMARY KEY (id));