import { Mastra } from '@mastra/core/mastra';
import { LibSQLStore } from '@mastra/libsql';
import { infinispanAgent } from './agents/infinispan-agent';

export const mastra = new Mastra({
  agents: { infinispanAgent },
  storage: new LibSQLStore({
    id: "mastra-storage",
    url: "file:./mastra.db",
  }),
});
