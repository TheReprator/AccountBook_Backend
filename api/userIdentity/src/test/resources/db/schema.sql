CREATE TABLE `country`
(
    `id`           LONG PRIMARY KEY AUTO_INCREMENT      NOT NULL,
    `name`         VARCHAR(255) UNIQUE                  NOT NULL CHECK (LENGTH(`name`) > 3),
    `callingcode`  LONG UNIQUE                      NOT NULL CHECK(`callingcode` > 0),
    `shortcode`    varchar UNIQUE                       NOT NULL CHECK (LENGTH(`shortcode`) > 0)
);

CREATE TABLE `user_login_data`
(
    `userid`            LONG PRIMARY KEY AUTO_INCREMENT         NOT NULL,
    `phoneNumber`       VARCHAR(255)                            NOT NULL,
    `phonecountryid`    integer                                 NOT NULL,
    `phoneOtp`          integer                                 ,
    `isphoneverified`   boolean                                 DEFAULT false,
    `usertype`          ENUM ('admin', 'owner', 'employee')     DEFAULT 'owner',
    `refreshtoken`      VARCHAR(1000)                            ,
    `creationtime`      TIMESTAMP                               DEFAULT CURRENT_DATE,
    `updatetime`        TIMESTAMP                               DEFAULT CURRENT_DATE,
    `otp_count`         integer                                 not null default 0,
    CONSTRAINT fk_countryId FOREIGN KEY(phoneCountryId) REFERENCES country(id),
    CONSTRAINT max_continous_otp_failure CHECK (otp_count <= 14)
);