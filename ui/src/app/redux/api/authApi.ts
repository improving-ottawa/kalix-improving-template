import {getClient} from "./client";
import {DoNothingTwiceCommand} from "../../../generated/com/example/gateway/domain/gateway_commands_pb";
import {DoNothingTwiceResponse} from "../../../generated/com/example/gateway/domain/gateway_responses_pb";

export function sendDoNothingTwiceCommand(req: DoNothingTwiceCommand) {
    var deadline = new Date();
    deadline.setSeconds(deadline.getSeconds() + 30);

    return new Promise<{ doNothingTwice: DoNothingTwiceResponse }>((resolve, reject) => {
        getClient().then(client => {
            client.doNothingTwice(req, {
                    //TODO: uncomment for routes that are authorized using JWT
                    //authorization: 'Bearer ' + window.BEARER_TOKEN,
                    deadline: deadline.getTime().toString()
                },
                (err, response) => {
                    if (err) {
                        console.error(`Unexpected error sending DoNothingTwice: code = ${err.code}` +
                            `, message = "${err.message}"`)
                        reject(err)
                    } else {
                        const resp = {doNothingTwice: response}
                        console.log(resp)
                        resolve(resp)
                    }
                })
        }).catch(err => {
            console.error(
                `Unexpected error creating client: code = ${err.code}` +
                `, message = "${err.message}"`
            )
            reject(err)
        })

    });

}
