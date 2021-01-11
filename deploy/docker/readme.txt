1、根据Dockerfile生成镜像：docker build -t bookstore:v1 .

2、运行镜像文件：docker run -itd -p 8080:8080 bookstore:v1 /bin/bash

3、查看所属容器：docker ps -a

4、访问url:  浏览器访问或者CURL -L http://localhost:8080/v2/catalog -u admin:supersecret
	