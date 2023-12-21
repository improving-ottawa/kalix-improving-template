import {getConfig} from "../../config";
import {
    AuthenticationServiceClient
} from "../../../generated/com/example/gateway/api/Authentication_serviceServiceClientPb";
import {GatewayClient} from "../../../generated/com/example/gateway/api/Gateway_actionServiceClientPb";
import Cookies from "cookies-ts";
import {AppIdentity} from "../../../generated/com/example/gateway/domain/gateway_responses_pb";

let cachedGatewayClient: GatewayClient | undefined = undefined

let cachedAuthClient: AuthenticationServiceClient | undefined = undefined

export const getGatewayClient = async () => {
    const config = await getConfig()
    if (!cachedGatewayClient) {
        cachedGatewayClient = new GatewayClient(config.exampleApiBaseUrl, null, {withCredentials: true})
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

export const getCsrfToken = () => {
    const token = sessionStorage.getItem('csrfToken')
    return token ?? ""
}
