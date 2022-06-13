create table if not exists `CUSTOMER`(
    `id` bigint(20) not null AUTO_INCREMENT,
    `username` varchar(255) not null ,
    `password` varchar (255) not null ,
    `jurisdiction` int(32) not null,
    primary key(`id`)
);
create table if not exists `DETAILBILL`(
    `billId` bigint(20) not null AUTO_INCREMENT,
    `userId` bigint(20),
    `isPay`  boolean,
    `chargingType` varchar (255),
     `startRquestTime`Timestamp default null,
     `startDate`Timestamp default null,
     `endDate`Timestamp default null,
     `totalFee` float(32),
    primary key(`billId`)
);