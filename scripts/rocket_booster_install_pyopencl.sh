#!/bin/bash

# A script to install pyopencl on booster nodes of rocket.hpc.ut.ee

# Load modules
module purge
module load intel_parallel_studio_xe_2015
module load python-2.7.6
# module load python-3.4.0 

cd $HOME
mkdir pyopenclboosterAndrii
cd pyopenclboosterAndrii/
# Get Mako templates
wget https://pypi.python.org/packages/7a/ae/925434246ee90b42e8ef57d3b30a0ab7caf9a2de3e449b876c56dcb48155/Mako-1.0.4.tar.
gz#md5=c5fc31a323dd4990683d2f2da02d4e20
tar -xvf Mako-1.0.4.tar.gz
cd Mako-1.0.4/
python setup.py install --user
cd ..

wget https://pypi.python.org/packages/source/p/pyopencl/pyopencl-2015.1.tar.gz
tar xfz pyopencl-2015.1.tar.gz
cd pyopencl-2015.1

# Download OpenCL 1.2 headers
mkdir -p include/CL
cd include/CL
wget https://github.com/KhronosGroup/OpenCL-Headers/blob/opencl21/{opencl,cl_platform,cl,cl_ext,cl_gl,cl_gl_ext}.h
cd ../..

# add six as install requirement to pyopencl
sed -i -- 's/"pytools>=2014.2",/"six>=1.4.0","pytools>=2014.2",/g' setup.py

# configure to use the downloaded headers
python configure.py --cl-inc-dir="$(pwd)/include" --cl-lib-dir="/opt/intel/intel/opencl/"
python setup.py install --user
cd ..

# setup paths
export OPENCL_VENDOR_PATH="/opt/intel/intel"

# Test installation works
#git clone https://github.com/andreyrozumnyi/intro-to-javacl-and-pyopencl
#git clone https://github.com/poolik/intro-to-javacl-and-pyopencl
#wget https://github.com/poolik/intro-to-javacl-and-pyopencl/archive/master.zip
#unzip master.zip
wget https://github.com/JaakTree/pattern_matching/archive/test.zip
unzip test.zip
# test code
python pattern_matching-test/pyopencl/naive/naive_pocl.py 

