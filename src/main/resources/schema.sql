-- drop table `CUSTOMER`;
create table if not exists `CUSTOMER`(
    `id` bigint(20) not null AUTO_INCREMENT,
    `username` varchar(255) not null ,
    `password` varchar (255) not null ,
    `jurisdiction` int(32) not null,
    primary key(`id`)
);
-- drop table `DETAIL`;
create table if not exists `DETAIL`(
    `billid` bigint(20) not null AUTO_INCREMENT,
    `userid` bigint(20),
    `ispay`  boolean,
    `chargingtype` varchar (255),
     `startrequesttime`Timestamp default null,
     `startdate`Timestamp default null,
     `enddate`Timestamp default null,
     `totalfee` float(32),
    primary key(`billid`)
);
-- drop table `BILL`;
create table if not exists `BILL`(
    `billid` bigint(20) not null AUTO_INCREMENT,
    `userid` bigint(20),
    `startdate`Timestamp default null,
    `enddate`Timestamp default null,
    `totalfee` float(32),
    primary key(`billid`)
);