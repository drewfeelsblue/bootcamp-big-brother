package config

final case class HttpServerConfig(
    host: Host,
    port: Port
) extends DetailedToString
