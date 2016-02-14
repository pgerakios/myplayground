
Instructions:

1.Install "precise32"

	 vagrant box add precise32 http://files.vagrantup.com/precise32.box

2. Install machines (1 load balancer at 10.0.0.10 , 2 workers 10.0.0.{11,12} all running nginx)

	mv Vagrantfile Vagrantfile1

	vagrant init  

	mv Vagrantfile1 Vagrantfile

	vagrant up


3. Test: (beware of browser caching, use command line tools instead)


	 wget http://10.0.0.10 -O -
	 wget http://10.0.0.10 -O -



