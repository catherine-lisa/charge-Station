create table if not exists `CUSTOMER`(
    `id` bigint(20) not null AUTO_INCREMENT,
    `username` varchar(255) not null ,
    `password` varchar (255) not null ,
    `jurisdiction` int(32) not null,
    primary key(`id`)
);
