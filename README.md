# DEWE.v2

DEWE v2 is a pulling-based workflow execution framework designed with public clouds (for example, AWS) as the target execution environment. With AWS EC2, a homogenous computing environment can be creating by launching a set of EC2 instances with the same instance type in the same availability zone (probably in the same placement group). Statistically, these EC2 instances have the same computing resource in terms of CPU, memory, storage, and networking. DEWE v2 takes advantages of this homogeneity to reduce the scheduling overhead in traditional workflow execution frameworks (such as Pegasus + HTCondor) with a pulling approach. Test results with five 6.0 degree Montage workflows running in parallel indicates that DEWE v2 can achieve 80% speed up as compared to the Pegasus + HTCondor solution on a single node cluster.

DEWE v2 was first published on the 44th International Conference on Parallel Processing (ICPP-2015) in Beijing (China) in the following paper:

Qingye Jiang, Young Choon Lee, Albert Y. Zomaya, “Executing Large Scale Scientific Workflow Ensembles in Public Clouds“, 44th International Conference on Parallel Processing (ICPP-2015), Beijing, September 2015

As a cloud-centric workflow execution framework, you don't need to install DEWE v2 to run your own workflows. Instead, you launch a DEWE v2 cluster with one or more EC2 instances to run your workflows. You can get started with DEWE v2 following the instructions in this tutorial:

http://www.qyjohn.net/?p=3981

