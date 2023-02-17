# sukatamak Galaxio bot
### Repositori ini dibuat untuk memenuhi Tugas Besar 1 Strategi Algoritma

## Authors
|         Nama              |   NIM    |
|---------------------------|----------|
| Melvin Kent Jonathan      | 13521052 |
| Daniel Egiant Sitanggang  | 13521056 |
| Hobert Anthony Jonatan    | 13521079 |

## About our Strategy 
Bot ini mengimplementasikan algoritma greedy untuk memenangkan permainan dengan mempertimbangkan langkah greedy dalam berbagai komponen. Langkah greedy dalam tiap komponen kemudian digabungkan untuk dibuat pembobotan manakah langkah greedy yang harus diambil dalam situasi tertentu. Bot yang kami buat menitikberatkan langkah greedy defensif sebelum memulai untuk menyerang player lain. 

## Program Requirements 
- JDK 11 (recommended) 
- Apache Maven 3.9.0 (stable version)
- IntelliJ IDEA
- .NET Core 3.1

## How to build 
Dalam repository ini, sudah ada bot yang telah di build dalam folder target dengan nama file sukatamak.jar, namun jika Anda ingin melakukan build kembali secara manual, Anda perlu terlebih dahulu melakukan download zip file [starter-pack](https://github.com/EntelectChallenge/2021-Galaxio/releases/tag/2021.3.2). Setelah itu unzip file tersebut.

Kemudian clone repositori ini di direktori `starter-pack/starter-pack/starter-bots/JavaBot`. Selanjutnya buka Command Prompt dan arahkan ke direktori `starter-pack/starter-pack/starter-bots/JavaBot`. Jalankan command `mvn clean package`, maka akan muncul hasil build bot dengan nama *sukatamak.jar* pada folder `target`.

