# Kafka Beginners Course Notes

### Kafka CLI commands
* **Start the Zookeeper server**:
Move to Kafka directory
```
bin/zookeeper-server-start.sh config/zookeeper.properties
```

* **Start Kafka**:
```
bin/kafka-server-start.sh config/server.properties
```

* **kafka-topics**: Create, delete, describe, or change a topic.
  * Create a topic:
   ```
   bin/kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic name_of_topic --create --partitions 3 --replication-factor 1
   ```
  * List the topics:
  ```
  bin/kafka-topics.sh --zookeeper 127.0.0.1:2181 --list
  ```
  * Get more information about a particular topic:
  ```
  bin/kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic name_of_topic --describe
  ```
  * Delete a topic:
  ```
  bin/kafka-topics.sh --zookeeper 127.0.0.1:2181 --topic name_of_topic --delete
  ```
  
* **kafka-console-producer**: Read data from standard input and publish it to Kafka.
  * Launch a Kafka Console Producer:
   ```
   bin/kafka-console-producer.sh --bootstrap-server 127.0.0.1:9092 --topic name_of_topic 
   ```
  * Note: If you start publishing to a topic that doesn't exist, Kafka will automatically create that topic for you, with the default of 1 partition and replication factor 1. And this is usually not what you want, you should have higher replication factor and ahigher partition count. Always create a topic before producing to it, otherwise it will get created with some defaults that are usually not good.
    * You can change the defaults if you want, by editing the Log basics section of server.properties file:
  ```
  nano config/server.properties
  ```
  
* **kafka-console-consumer**: The console consumer is a tool that reads data from Kafka and outputs it to standard output.
  * Launch a console-consumer, that only reads the future messages:
   ```
   bin/kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic name_of_topic 
   ```
  * Read all the messages in a topic:
  ```
  bin/kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic name_of_topic --from-beginning
  ```
  * Make the consumer be part of a consumer group:
  ```
  bin/kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 --topic name_of_topic --group group_name
  ```
  
* **kafka-consumer-groups**: List all consumer groups, describe a consumer group, delete consumer group info, or reset consumer group offsets.
  * List all of the consumer groups:
   ```
   bin/kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --list 
   ```
  * Get more information about a particular consumer group: (useful to see which applications or which machine is consuming on to your topic)
  ```
  bin/kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --describe --group group_name
  ```
  * Resetting offsets: (Options are: --to-datetime, --by-period, --to-earliest, --to-latest, --shift-by, --from-file, --to-current)
  ```
  bin/kafka-consumer-groups.sh --bootstrap-server 127.0.0.1:9092 --group group_name --reset-offsets --to-earliest --execute --topic topic_name
  ```
  
### Kafka Theory

#### Topics, Partitions and Offsets
* **Topics**: a particular stream of data 
  * Similar to a table in relational databases (without all the constraints)
  * You can have as many topics as you want
  * A topic is identified by is <ins>name</ins>
  * Topics are split in <ins>partitions</ins>
* **Partitions**:
  * Each message is ordered
  * Each message within a partition gets an incremental ID, called <ins>offset</ins>
  * Offset only have a meaning for a specific partition. E.g. offset 3 in partition 0 doesn't represent the same data as offset 3 in partition 1.
  * Order is guaranteed only within a partition (not accross partitions)
  * When creating a topic you need to specify how many partitions you want. 
  * Data is kept in Kafka only for a limited time (default is one week)
  * Once the data is written to a partition, <ins>it can't be changed</ins> (immutability)
  * Data is assigned randomly to a partition unless a <ins>key</ins> is provided.
  
#### Brokers
* A Kafka cluster is composed of multiple brokers (servers)
* Each broker is identified with its ID (integer)
* Each broker contains certain topic partitions.
* After connecting to any broker (called a bootstrap broker), you will be connected to the entire cluster.
* A good number to start is 3 brokers, but some big clusters have over a 100 brokers.

#### Topic replication factor
* When you create a topic you need to decide on a replication factor.
  * Topics should have a replication factor > 1 (usually between 2 and 3)
* By replicating you ensure that if a broker is down, another broker can serve the data.
* You cannot create a topic with a replication factor greater than the number of brokers that you have.

#### Concept of Leader for a Partiton (All of this is handled by Kafka)
* **At any time, only ONE broker can be a leader for a given partition**
* Only that leader can receive and serve data for a partition.
* The other brokers will just be passive replicas and they will just synchronize the data.
* Therefore, each partition has 1 leader and multiple ISR (in-sync replica)
* Zookeeper descides which broker is leader and which are ISR
* If a leader goes down, an ISR will become a leader

#### Producers
* Producers write data to topics (which is made of partitions)
* Producers automatically know to which broker and partition to write to. You don't need to specify that.
* In case of Broker failures, Producers will automatically recover.
* The load is balanced to many brokers thanks to the number of partitions.
* Producers can choose to send a **key** with the message 
  * The key can be anything you want. (string, number, etc...)
  * If key=null (the key is not sent), then data will just be sent round robin to the brokers.
  * If a key is sent, they all messages for that key will always go to the same partition. That's a Kafka guarantee.
  * A key is basically sent if you need message ordering for a specific field.
* Producers can choose to **receive acknowledgements** of data writes:
  *  <ins>acks=0</ins>: Producer won't wait for acknowledgement (possible data loss)
  *  <ins>acks=1</ins>: Producer will wait for leader acknowledgement (limited data loss) (the default)
  *  <ins>acks=all</ins>: Leader + all of the replicas acknowledgement (no data loss)
  
#### Consumers
* Consumers read data from a topic (identified by name)
* Consumers automatically know which broker to read from.
* In case of broker failures, consumers know how to recover.
* Data is read in order, <ins>within each partition</ins>

#### Consumer Groups
* Basically, you can have a lot of consumers. (consumer will be a Java application or whatever language you're using) and 
* Consumers will read data in <ins>groups</ins>.
* Each consumer within a group reads from exclusive partitions.
* Consumers will automatically use a GroupCoordinator and a ConsumerCoordinator to assign consumers to partitions. This is not something you have to program, it's a mechanism already implemented in Kafka.
* If you have more consumers than partitions, some consumers will be inactive. So, if you want to have a high number of consumers, you need to have a high number of partitions. <ins> This is a decision you have to make</ins> 

#### Consumer Offsets
* **Kafka** stores the offsets at which a consumer group has been reading.
* The offsets are comitted live, in a **topic** called **__consumer_offsets**.
* When a consumer in a group has processed data received from Kafka, it should be comitting the offsets
* If a consumer dies, it will be able to read back from where it left off thanks to the comitted consumer offstes!
* Comitting offsets implies something called <ins>delivery semantics</ins>
  * Consumers choose when to commit offsets.
  * **There are 3 delivery semantics**:
    * **At most once**:  
      * Offsets are committed as soon as the message is received.
      * If the processing goes wrong, the message will be lost (it won't be read again).
    * **At least once** (usually preferred): 
      * Offsets are committed after the message is processed.
      * If the processing goes wrong, the message will be read again.
      * This can result in duplicate processing of messages. Make sure your processing is <ins>idempotent</ins> (i.e. processing again the messages won't impact your systems)
    * **Exactly once**:
      * Can be only be achieved for Kafka => Kafka workflows, using Kafka Streams API
      * For Kafka => External sytem workflows, use an <ins>idempotent</ins> consumer.
      
#### Kafka Broker Discovery
* Producer and Consumers can automatically figure out which producer, which broker they can send data to. How does this work?
  * Every Kafka broker is also called a "bootstrap server"
  * That means that **you only need to connect to one broker**, and you will be connected to the entire cluster.
  * Each broker knows about all of the other brokers, topics and partitions (metadata)
  * When the Kafka client (producer or consumer) connects to a broker and the connection is established, the client will automatically, behind the scenes, do something called a metadata request. And the broker will send a list of all the brokers, and their IPs, etc. And when it starts producing or consuming, it knows to which broker it needs to connect automatically. 

#### Zookeeper
* Zookeeper manages Brokers (keeps a list of them)
* Zookeeper helps in performing leader election for partitions.
* Zookeeper will also send notifications to Kafka in case of changes (e.g. new topic, broker dies, broker comes up, delete topics, etc...)
* **Kafka can not work without Zookeeper**. When we start Kafka, we first need to start Zookeeper.
* Zookeeper by design, in production, operates with an odd number of servers (3, 5, 7)
* Zookeeper has also a concept of leaders and followers. One leader handles writes and the rest of the servers are followers and handle reads.
* The consumers and Producers don't to Zookeeper, they write to Kafka. Kafka just manages all metadata in Zookeeper.
* Zookeeper does not store consumer offsets (After Kafka v0.10), they are stored in a Kafka topic.
* Basically, your Kafka cluster will be connected to a Zookeeper cluster. And automatically it will understand when brokers are down, when topics are created, etc.

#### Kafka Guarantees
* Messages are appended to a topic-partition in the order they are sent.
* Consumers read messages in the order stored in a topic-partition.
* With a replication factor of N, producers and consumers can tolerate up to N-1 brokers being down. This is why a replication factor of 3 is a good idea:
  * Allows for one broker to be taken down for maintenance.
  * Allows for another broker to be taken down unexpectedly.
* As long as the number of partitions remains constant for a topic (no new partitions), the same key will always go to the same partition.

  
