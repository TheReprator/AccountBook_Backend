CREATE TABLE `country`
(
    `id`           LONG PRIMARY KEY AUTO_INCREMENT      NOT NULL,
    `name`         VARCHAR(255) UNIQUE                  NOT NULL CHECK (LENGTH(`name`) > 3),
    `callingcode`  LONG UNIQUE                      NOT NULL CHECK(`callingcode` > 0),
    `shortcode`    varchar UNIQUE                       NOT NULL CHECK (LENGTH(`shortcode`) > 0)
);