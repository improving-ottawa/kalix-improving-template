import {TextField} from "@mui/material";
import {setUsername} from "../redux/slices/usernameSlice";
import {useAppDispatch} from "../redux/hooks";

const ChangeName = () => {

    const dispatch = useAppDispatch();

    return <TextField onChange={e => dispatch(setUsername(e.target.value))} required id="outlined-basic" label="Outlined" variant="outlined" />;
};

export default ChangeName;