# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table admin (
  id                        bigint not null,
  name                      varchar(255),
  email                     varchar(255),
  password                  varchar(255),
  phoneNumber               varchar(45) not null,
  constraint uq_admin_email unique (email),
  constraint pk_admin primary key (id))
;

create table dosen (
  id                        bigint not null,
  name                      varchar(255),
  nrp                       varchar(255),
  constraint pk_dosen primary key (id))
;

create table jurusan (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_jurusan primary key (id))
;

create table mahasiswa (
  id                        bigint not null,
  name                      varchar(255),
  image                     TEXT,
  nim                       varchar(255),
  jurusan_id                bigint,
  angkatan                  varchar(255),
  email                     varchar(255),
  birth                     timestamp,
  no_phone                  varchar(45) not null,
  address                   varchar(255),
  constraint uq_mahasiswa_email unique (email),
  constraint pk_mahasiswa primary key (id))
;

create table mata_kuliah (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_mata_kuliah primary key (id))
;

create sequence admin_seq;

create sequence dosen_seq;

create sequence jurusan_seq;

create sequence mahasiswa_seq;

create sequence matkul_seq;

alter table mahasiswa add constraint fk_mahasiswa_jurusan_1 foreign key (jurusan_id) references jurusan (id);
create index ix_mahasiswa_jurusan_1 on mahasiswa (jurusan_id);



# --- !Downs

drop table if exists admin cascade;

drop table if exists dosen cascade;

drop table if exists jurusan cascade;

drop table if exists mahasiswa cascade;

drop table if exists mata_kuliah cascade;

drop sequence if exists admin_seq;

drop sequence if exists dosen_seq;

drop sequence if exists jurusan_seq;

drop sequence if exists mahasiswa_seq;

drop sequence if exists matkul_seq;

