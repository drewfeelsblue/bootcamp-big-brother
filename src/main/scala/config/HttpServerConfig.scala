package config

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.IPv4

case class HttpServerConfig(
    host: String Refined IPv4,
    port: Port
) extends DetailedToString
