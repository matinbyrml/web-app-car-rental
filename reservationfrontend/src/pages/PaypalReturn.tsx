import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

const PaypalReturn: React.FC = () => {
    const [params] = useSearchParams();
    const navigate = useNavigate();

    useEffect(() => {
        (async () => {
            const token = params.get('token');
            if (!token) { navigate('/reservations?paid=ko'); return; }

            try {
                const res = await fetch(`/api/v1/orders/return?token=${token}`);
                if (!res.ok) throw new Error();
                navigate('/reservations?paid=ok', { replace:true });
            } catch {
                navigate('/reservations?paid=ko', { replace:true });
            }
        })();
    }, []);

    return <p>Finishing payment…</p>;
};

export default PaypalReturn;
