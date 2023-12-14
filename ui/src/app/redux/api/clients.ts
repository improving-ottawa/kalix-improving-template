import {getConfig} from "../../config";
import {GatewayClient} from "../../../generated/com/example/gateway/api/Gateway_actionServiceClientPb";
import {LoginTokenClient} from "../../../generated/com/example/gateway/api/Login_serviceServiceClientPb";
import {
    AuthenticationServiceClient
} from "../../../generated/com/example/gateway/api/Authentication_serviceServiceClientPb";

let cachedGatewayClient: undefined | GatewayClient = undefined
let cachedAuthClient: undefined | AuthenticationServiceClient = undefined
export const getGatewayClient = async () => {
    if (cachedGatewayClient) return cachedGatewayClient
    else {
        const config = await getConfig()
        cachedGatewayClient = new GatewayClient(config.exampleApiBaseUrl)
        return cachedGatewayClient
    }
}

export const getAuthClient = async () => {
    if (cachedAuthClient) return cachedAuthClient
    else {
        const config = await getConfig()
        cachedAuthClient = new AuthenticationServiceClient(config.exampleApiBaseUrl)
        return cachedAuthClient
    }
}
