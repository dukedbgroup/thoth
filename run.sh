#!/bin/bash

declare -a slaves=("ec2-54-152-214-112.compute-1.amazonaws.com" "ec2-54-236-202-239.compute-1.amazonaws.com" "ec2-54-236-201-109.compute-1.amazonaws.com " "ec2-54-236-210-149.compute-1.amazonaws.com" "ec2-54-236-209-146.compute-1.amazonaws.com" "ec2-54-146-203-1.compute-1.amazonaws.com" "ec2-54-221-66-229.compute-1.amazonaws.com" "ec2-54-236-196-138.compute-1.amazonaws.com")

declare -a classes=("SparkPageRank")
declare -a scales=("large")
declare -a executor_memory=(1 2 3 4 5 6 7)
declare -a num_executors=(1 2 3 4 5 6 7 8)

#for dns in "${slaves[@]}"
#do
  #scp -i ~/.ssh/amy-us-east-1.pem  thoth-shell-1.0-SNAPSHOT.jar root@$dns:/root
  #echo $dns
  #ssh -i ~/.ssh/amy-us-east-1.pem root@$dns 'killall pidstat' #java -cp thoth-shell-1.0-SNAPSHOT.jar
#done

for class in "${classes[@]}"
do
	for scale in "${scales[@]}"
	do
		for exec_mem in "${executor_memory[@]}"
		do
			for num_exec in "${num_executors[@]}"
			do
        if [ $(($num_exec * $exec_mem)) -lt 5 ]; then
				  continue
        fi
        declare s3_path=""
				if [ "$class" = "WordCount" ]; then
				  s3_path="Wordcount/Input/*" 
				elif [ "$class" = "Sort" ]; then
					s3_path="Sort/Input/*"
				elif [ "$class" = "SparkPageRank" ]; then
					s3_path="Pagerank/Input/edges/*"
				fi;

				for dns in "${slaves[@]}"
				do
				  ssh -i ~/.ssh/amy-us-east-1.pem root@$dns 'rm -r pidstat'
				  ssh -i ~/.ssh/amy-us-east-1.pem root@$dns 'mkdir pidstat'
				  ssh -i ~/.ssh/amy-us-east-1.pem root@$dns 'java -cp thoth-shell-1.0-SNAPSHOT.jar edu.duke.thoth.PidstatCollector' &  #java -cp thoth-shell-1.0-SNAPSHOT.jar
				  ssh -i ~/.ssh/amy-us-east-1.pem root@$dns 'rm -rf /mnt/yarn-local/usercache'
        done

				timestamp=$(date +%s)
        echo "[SPARK SUBMIT] "$class" scale "$scale
				rm /root/out.txt
        spark-submit --class org.apache.spark.examples.$class \
        --num-executors $num_exec \
        --executor-memory $exec_mem"g" \
				--conf "spark.executor.extraJavaOptions=-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:/root/javagc-"$timestamp \
				--master yarn --deploy-mode client \
				/root/spark-examples_2.11-2.0.1.jar \
				s3n://hibench-inputs/$scale/HiBench/$s3_path
        
				for dns in "${slaves[@]}"
				do
				 ssh -i ~/.ssh/amy-us-east-1.pem root@$dns 'mkdir pidstat' 
				 ssh -i ~/.ssh/amy-us-east-1.pem root@$dns 'killall pidstat' 
				done
				
				#copy instrumentation result

				for dns in "${slaves[@]}"
				do
           mkdir -p /root/result/$scale/$class/num_exec-$num_exec/exec_mem-$exec_mem/$timestamp/$dns
				   scp -i ~/.ssh/amy-us-east-1.pem -r root@$dns:/root/pidstat /root/result/$scale/$class/num_exec-$num_exec/exec_mem-$exec_mem/$timestamp/$dns 
            
				   #copy GC
				   scp -i ~/.ssh/amy-us-east-1.pem root@$dns:/root/javagc-$timestamp /root/result/$scale/$class/num_exec-$num_exec/exec_mem-$exec_mem/$timestamp/$dns
			     ssh -i ~/.ssh/amy-us-east-1.pem root@$dns 'kill -9 $(jps | grep PidstatCollector | cut -d " " -f1)'
         done
        
				killall -9 ssh
        killall -9 scp
			
			done
		done
	done
done

# TODO
#copy spark log			
#copy yarn log

