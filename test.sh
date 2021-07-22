#!/usr/bin/env bash

# 토픽 목록 확인
dco exec kafka /opt/kafka/bin/kafka-topics.sh --zookeeper zookeeper --list

# 토픽 정보 확인
dco exec kakfa /opt/kafka/bin/kafka-topics.sh --describe --zookeeper zookeeper --topic products

# 토픽 메세지 (모두)확인
dco exec kakfa /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic products --from-beginning --timeout-ms 1000

# 토픽의 특정 파티션 메세지 (모두)확인
dco exec kakfa /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic products --from-beginning --timeout-ms 1000 --partition 1

# HEALTH 체크
curl -s localhost:8080/actuator/health | jq -r .status

# 테스트
## 기본 RabbitMQ(no partition) 사용
unset COMPOSE_FILE && \
./test-em-all.bash start stop

## RabbitMQ Partition(2) 사용
export COMPOSE_FILE=docker-compose-partitions.yml && \
./test-em-all.bash start stop && \
unset COMPOSE_FILE

## Kafka Partition(2) 사용
export COMPOSE_FILE=docker-compose-kafka.yml && \
./test-em-all.bash start stop && \
unset COMPOSE_FILE

## Eureka API
curl -H "accept:application/json" localhost:8761/eureka/apps -s | jq -r '.applications.application[].instance[].instanceId'

## Loadbalancer Test
curl localhost:8080/product-composite/2 -s | jq -r .serviceAddresses.rev

## Scale 조정
docker-compose up -d --scale review=2 --scale eureka=0

## 포트 확인
docker-compose ps gateway eureka product-composite product recommendation review

## 에지서버 라우트 경로 확인
curl localhost:8080/actuator/gateway/routes -s | jq '.[] | {"\(.route_id)":"\(.predicate)"}'

## 도커 로그 확인
dco logs -f --tail=0 gateway

## 검색서버에 등록된 인스턴스 목록 조회
curl -H "accept:application/json" localhost:8080/eureka/api/apps -s | jq -r '.applications.application[].instance[].instanceId'

## 호스트 헤더 기반 라우팅 테스트
curl localhost:8080/headerrouting -H "HOST: i.feel.lucky:8080"

curl localhost:8080/headerrouting -H "HOST: im.a.teapot:8080"

## 인증
http://gateway:8080/oauth/authorize?response_type=code&client_id=reader&scope=product:read&redirect_uri=my.redirect.uri

code=3MCH9w

curl -k http://reader:secret@gateway:8080/oauth/token -d grant_type=authorization_code -d client_id=reader -d redirect_uri=http://my.redirect.uri -d code=3MCH9w

curl -k http://reader:secret@gateway:8080/oauth/introspect -d token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJtYWdudXMiLCJleHAiOjIyMjY3NDg1MDUsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJqdGkiOiJmMTZjYmU1MC05Nzk3LTQ5OTQtOGQ5Ni01YzkxMWZmYWI1NGQiLCJjbGllbnRfaWQiOiJyZWFkZXIiLCJzY29wZSI6WyJwcm9kdWN0OnJlYWQiXX0.bm827VB9jZBa4iHCLkWw8LuYiQFQaubqiBMsfKoC7fvpOcG2Vl3GgY5Xws3m2BKY5lcnHPKRTAZGq7LfLb8OQN79eX9r-wBZ_YHoCa-yoieWbQwj5-UsjG7QPEIhPyT0U-EonouRyrB5-nxAnjh0Ri-ZeNNoBiumaYVWVFiGmi0pm0UwZUj-BMEhyJlyykVfRXDuOcAI3_7rxc4cRX4uN39XDJEUpr9zMIC4KMQIIzz3e-KnItti5-NNz_4E8DxEEK9u39PvTZMTtuBfF4cR8Xx7IEITM7WLZnskuJ6kUy-SGM1nHDuyvAqqUgGMBZjDaXBQ_jOsQ-2AZAYdI0rAXQ