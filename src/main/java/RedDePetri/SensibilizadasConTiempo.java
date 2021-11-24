package RedDePetri;


    public class SensibilizadasConTiempo {
        private int alpha;
        private int beta;
        private long id;
        private boolean flag;// todo id y flag hay que implementarlo
        private long startTime;
        private boolean esperando;

        SensibilizadasConTiempo(int alpha, int beta){
            this.alpha = alpha;
            this.beta = beta;
            this.flag = false;
            this.startTime = -1;
            this.id = -999999;
            this.esperando=false;
        }


    public boolean testVentanaTiempo() {
        long ahora = System.currentTimeMillis();
        return ((ahora - startTime) >= alpha )
                && ((ahora - startTime) < beta);
    }


        public void setNuevoTimeStamp( ) { //todo resetea o se sensibiliza la transicion? hay que actualizar todos los vectores?
            flag=false;
            startTime=-1;
            id=-999999;
        }
        public boolean isEsperando() {
            return esperando;
        }

        public void setEsperando() {
            this.esperando = true;
        }

        public int getAlpha() {
            return alpha;
        }

        public long getStartTime() {
            return startTime;
        }

    }
