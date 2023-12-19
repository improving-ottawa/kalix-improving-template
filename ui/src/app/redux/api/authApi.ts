import {BeginAuthenticationRequest} from "../../../generated/com/example/gateway/api/authentication_service_pb";
import {getAuthClient} from "./clients";
import {HttpBody} from "../../../generated/google/api/httpbody_pb";

export const sendBeginAuthenticationRequest = async (req: BeginAuthenticationRequest) => {
    var deadline = new Date();
    deadline.setSeconds(deadline.getSeconds() + 30);

    const client = await getAuthClient()

    return new Promise<HttpBody>((resolve, reject) => {
        client.oidcAuthentication(req, {
                //TODO: uncomment for routes that are authorized using JWT
                //authorization: 'Bearer ' + window.BEARER_TOKEN,
                deadline: deadline.getTime().toString()
            },
            (err, response) => {
                if (err) {
                    console.error(`Unexpected error sending BeginAuthenticationRequest: code = ${err.code}` +
                        `, message = "${err.message}"`)
                    reject(err)
                } else {
                    console.log(response)
                    resolve(response)
                }
            })
    });

}

