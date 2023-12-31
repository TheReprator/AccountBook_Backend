CREATE TABLE `country`
(
    `id`           LONG PRIMARY KEY AUTO_INCREMENT      NOT NULL,
    `name`         VARCHAR(255) UNIQUE                  NOT NULL CHECK (LENGTH(`name`) > 3),
    `isocode`      LONG UNIQUE                          NOT NULL CHECK(`isocode` > 0),
    `shortcode`    varchar UNIQUE                       NOT NULL CHECK (LENGTH(`shortcode`) > 0)
);

CREATE TABLE `user_login_data`
(
    `userId`            LONG PRIMARY KEY AUTO_INCREMENT         NOT NULL,
    `phoneNumber`       VARCHAR(255)                            NOT NULL,
    `phoneCountryId`    integer                                 NOT NULL,
    `phoneOtp`          integer                                 ,
    `isPhoneVerified`   boolean                                 DEFAULT false,
    `userType`          ENUM ('admin', 'owner', 'employee')     DEFAULT 'owner',
    `refreshToken`      VARCHAR(255)                            ,
    CONSTRAINT fk_countryId
        FOREIGN KEY(phoneCountryId)
            REFERENCES country(id)
);
