import {useNavigate} from "react-router-dom";
import * as React from "react";
import Box from "@mui/material/Box";
import {Link} from "@mui/material";
import Button from "@mui/material/Button";
import {PropsWithChildren} from "react";

export interface ShippingBillingFooterProps {
    handleNext: () => void,
    handleBack: () => void,
    showBack: boolean
}

export default function ShippingBillingFooter(props: ShippingBillingFooterProps & PropsWithChildren) {
    const navigate = useNavigate()

    return (
        <React.Fragment>
            {props.children}
            <Box sx={{display: 'flex'}}>
                <Box sx={{display: 'flex', justifyContent: 'flex-start'}}>
                    <Link underline="none" onClick={() => navigate("/pricing")}
                          sx={{mt: 3, ml: 1}}>
                        {"Continue shopping"}
                    </Link>
                </Box>
                <Box sx={{display: 'flex', marginLeft: 'auto'}}>
                    {props.showBack && (
                        <Button onClick={props.handleBack} sx={{mt: 3, ml: 1}}>
                            Back
                        </Button>
                    )}
                    <Button
                        variant="contained"
                        onClick={props.handleNext}
                        sx={{mt: 3, ml: 1}}
                    >
                        Next
                    </Button>
                </Box>
            </Box>
        </React.Fragment>
    );
}

ShippingBillingFooter.defaultProps = {showBack: true}