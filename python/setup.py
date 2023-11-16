from setuptools import setup, find_packages

setup(
    name='Triplifier',
    version='0.1.0',
    author='Johan van Soest',
    author_email='j.vansoest@maastrichtuniversity.nl',
    packages=find_packages(),
    url='https://github.com/MaastrichtU-CDS/triplifier',
    license='Apache 2.0',
    description='A package to convert CSV and relational databases into RDF',
    long_description="A package to convert CSV and relational databases into RDF",
    install_requires=[
        "sqlalchemy",
        "rdflib",
        "requests",
        "click",
        "pandas",
        "git+https://github.com/jvsoest/datasources.git@dev"
    ],
)