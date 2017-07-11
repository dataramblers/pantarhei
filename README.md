# pantarhei

pantarhei reads in arbitrary textual resources and emits them in tokenized chunks. It could thus serve as a provider of "streamified" textual data e.g. to a Kafka, Flink or Spark cluster.

## Installation

`pantarhei` requires JDK 1.8+, git and SBT (simple build tool) to compile the source code.

The installation itself is as simple as this:

    git clone https://github.com/sschuepbach/pantarhei
    cd pantarhei
    sbt assembly

You should then get a "fat" JAR file in the `target/` folder.

## Usage

To run the application issue the following command:

    java -jar target/<jar-file> <options>

The following options are available:

    -i, --inputs <value>        Input files / URLs / directories to process (required). Argument can be used several times.
    -t, --token <value>         Tokens to be extracted: One of 'sentence', 'line', 'word', 'char'. Defaults to 'sentence'.
    -k, --kafka <value>         Kafka sink. Value must match <host>:<port>:<groupId>:<topic>. Off per default
    -s, --socket <value>        Connects to socket. Value must match <port>. Off per default
    -o, --stdout <value>        Send output to STDOUT. On per default
    -e, --emitFreq <value>      Emission frequency in milliseconds. Exact value or range (<ms>-<ms>). Defaults to 1000
    -n, --tokenNumber <value>   Number of tokens per emission. Exact value or range (<tpe>-<tpe>). Defaults to 1
    --help                      Prints this help text.

## Known bugs

- Socket sink is defunct at the moment
