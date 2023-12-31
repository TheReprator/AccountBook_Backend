CREATE TABLE `country`
(
    `id`           LONG PRIMARY KEY AUTO_INCREMENT      NOT NULL,
    `name`         VARCHAR(255) UNIQUE                  NOT NULL CHECK (LENGTH(`name`) > 3),
    `isocode`      LONG UNIQUE                          NOT NULL CHECK(`isocode` > 0),
    `shortcode`    varchar UNIQUE                       NOT NULL CHECK (LENGTH(`shortcode`) > 0)
);

CREATE TABLE `user_login_data`
(
    `userid`            LONG PRIMARY KEY AUTO_INCREMENT         NOT NULL,
    `phoneNumber`       VARCHAR(255)                            NOT NULL,
    `phonecountryid`    integer                                 NOT NULL,
    `phoneOtp`          integer                                 ,
    `isphoneverified`   boolean                                 DEFAULT false,
    `usertype`          ENUM ('ADMIN', 'OWNER', 'EMPLOYEE')     DEFAULT 'OWNER',
    `refreshtoken`      VARCHAR(255)                            ,
    `creationtime`      TIMESTAMP                               DEFAULT CURRENT_DATE,
    `updatetime`        TIMESTAMP                               DEFAULT CURRENT_DATE,
    CONSTRAINT fk_countryId
        FOREIGN KEY(phoneCountryId)
            REFERENCES country(id)
);
