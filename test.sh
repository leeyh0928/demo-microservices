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
export COMPOSE_FILE=docker-compose-partitions.yml && \
./test-em-all.bash start stop && \
unset COMPOSE_FILE

