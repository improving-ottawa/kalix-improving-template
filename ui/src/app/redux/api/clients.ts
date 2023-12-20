import {getConfig} from "../../config";
import {
    AuthenticationServiceClient
} from "../../../generated/com/example/gateway/api/Authentication_serviceServiceClientPb";
import {GatewayClient} from "../../../generated/com/example/gateway/api/Gateway_actionServiceClientPb";
import {jwtDecode} from "jwt-decode";
import Cookies from "cookies-ts";

let cachedGatewayClient: GatewayClient | undefined = undefined

let cachedAuthClient: AuthenticationServiceClient | undefined = undefined

export const getGatewayClient = async () => {
    const config = await getConfig()
    if (!cachedGatewayClient) {
        cachedGatewayClient = new GatewayClient(config.exampleApiBaseUrl)
    }
    return cachedGatewayClient
}

export const getAuthClient = async () => {
    const config = await getConfig()
    console.log(config)

    if (!cachedAuthClient) {
        cachedAuthClient = new AuthenticationServiceClient(config.exampleApiBaseUrl)
    }
    return cachedAuthClient
}

export const csrfHeader = () => {
    const token = sessionStorage.getItem('csrfToken')
    return 'X-CSRF-Token ' + (token ?? "")
}

export const decodedJwtToken = () => {
    const cookies = new Cookies()
    const cookie = cookies.get('authToken')
    return cookie ? jwtDecode(cookie) : null
}
