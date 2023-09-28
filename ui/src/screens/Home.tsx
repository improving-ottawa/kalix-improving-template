import {useAppSelector} from "../redux/hooks";
import {selectUsername} from "../redux/slices/usernameSlice";

const Home = () => {
    const greeting: string = "Hi " + useAppSelector(selectUsername) + "!";

    return <h1>{greeting}</h1>;
};

export default Home;